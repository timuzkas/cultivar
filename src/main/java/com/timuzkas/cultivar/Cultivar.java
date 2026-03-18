package com.timuzkas.cultivar;

import org.bukkit.plugin.java.JavaPlugin;

public class Cultivar extends JavaPlugin {

    private Database database;
    private CropManager cropManager;
    private ActionBarAnimator animator;
    private PipeManager pipeManager;
    private TeaBrewManager teaBrewManager;
    private RecipeRegistry recipeRegistry;
    private SoilManager soilManager;
    private PlayerStrainManager playerStrainManager;
    private HarvestBasketManager basketManager;
    private GrowerManager growerManager;
    private DryingRackManager dryingRackManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Init components
        database = new Database(this);
        try {
            database.connect();
        } catch (Exception e) {
            getLogger().severe(
                "Failed to connect to database: " + e.getMessage()
            );
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        cropManager = new CropManager(database);
        try {
            cropManager.loadAll();
        } catch (Exception e) {
            getLogger().severe("Failed to load crops: " + e.getMessage());
        }

        try {
            database.createSoilTable();
        } catch (Exception e) {
            getLogger().severe("Failed to create soil table: " + e.getMessage());
        }

        try {
            database.createPlayerStrainsTable();
        } catch (Exception e) {
            getLogger().severe("Failed to create player strains table: " + e.getMessage());
        }

        soilManager = new SoilManager(database);
        soilManager.loadFromDatabase();

        playerStrainManager = new PlayerStrainManager(database, this);

        growerManager = new GrowerManager(database, this);

        dryingRackManager = new DryingRackManager(this);

        animator = new ActionBarAnimator(this);
        pipeManager = new PipeManager();
        teaBrewManager = new TeaBrewManager();
        recipeRegistry = new RecipeRegistry(this);
        basketManager = new HarvestBasketManager(this, animator);

        // Register listeners
        getServer()
            .getPluginManager()
            .registerEvents(new CropPlaceListener(cropManager, animator, this), this);
        getServer()
            .getPluginManager()
            .registerEvents(new CropBreakListener(cropManager), this);
        CropInteractListener cropInteractListener = new CropInteractListener(cropManager, animator, this);
        cropInteractListener.setSoilManager(soilManager);
        cropInteractListener.setStrainManager(playerStrainManager);
        cropInteractListener.setBasketManager(basketManager);
        cropInteractListener.setGrowerManager(growerManager);
        getServer()
            .getPluginManager()
            .registerEvents(cropInteractListener, this);
        getServer()
            .getPluginManager()
            .registerEvents(new FurnaceListener(), this);
        getServer()
            .getPluginManager()
            .registerEvents(
                new TeaBrewListener(teaBrewManager, animator, this),
                this
            );
        getServer()
            .getPluginManager()
            .registerEvents(
                new PipeListener(pipeManager, animator, this),
                this
            );
        getServer()
            .getPluginManager()
            .registerEvents(new ChunkListener(cropManager), this);
        getServer()
            .getPluginManager()
            .registerEvents(new LootInjector(this), this);
        getServer()
            .getPluginManager()
            .registerEvents(new TeaDryingListener(this, animator), this);
        getServer()
            .getPluginManager()
            .registerEvents(new PlayerJoinListener(playerStrainManager, growerManager), this);
        getServer()
            .getPluginManager()
            .registerEvents(new FarmlandInteractListener(soilManager, animator), this);
        getServer()
            .getPluginManager()
            .registerEvents(new CraftingListener(), this);
        getServer()
            .getPluginManager()
            .registerEvents(new DryingRackListener(dryingRackManager, animator), this);

        // Register recipes
        recipeRegistry.register();

        // Register commands
        CultivarCommand command = new CultivarCommand(cropManager, animator, this);
        command.setStrainManager(playerStrainManager);
        command.setSoilManager(soilManager);
        command.setGrowerManager(growerManager);
        getCommand("cultivar").setExecutor(command);

        // Start tasks
        CropGrowthTask growthTask = new CropGrowthTask(cropManager, this);
        growthTask.setSoilManager(soilManager);
        growthTask.setGrowerManager(growerManager);
        growthTask.runTaskTimer(this, 0, 1200); // 60s
        new CropParticleTask(cropManager, this).runTaskTimer(this, 0, 600); // 30s
        new ProximityNotifyTask(cropManager, animator, this).runTaskTimer(
            this,
            0,
            200
        ); // 10s
        new SaveTask(cropManager).runTaskTimer(this, 0, 6000); // 5min
        new PipeMonitorTask(pipeManager, this).runTaskTimer(this, 0, 600); // 30s

        getLogger().info("Cultivar enabled!");
    }

    @Override
    public void onDisable() {
        // Save all
        try {
            if (cropManager != null) {
                cropManager.saveAll();
            }
            if (database != null) {
                database.disconnect();
            }
        } catch (Exception e) {
            getLogger().severe("Failed to save on disable: " + e.getMessage());
        }

        // Clear tasks
        if (animator != null) {
            animator.clearAll();
        }

        getLogger().info("Cultivar disabled!");
    }

    public ActionBarAnimator getAnimator() {
        return animator;
    }
}
