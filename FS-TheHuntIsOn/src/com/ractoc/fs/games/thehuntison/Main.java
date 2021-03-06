package com.ractoc.fs.games.thehuntison;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.ractoc.fs.games.thehuntison.appstates.StarFieldAppState;
import com.ractoc.fs.appstates.AiAppState;
import com.ractoc.fs.appstates.DamageAppState;
import com.ractoc.fs.appstates.FlightAppState;
import com.ractoc.fs.appstates.FlightControlAppState;
import com.ractoc.fs.appstates.SceneAppState;
import com.ractoc.fs.appstates.ShootingAppState;
import com.ractoc.fs.components.es.AiComponent;
import com.ractoc.fs.components.es.BoundedEntityComponent;
import com.ractoc.fs.components.es.CanMoveComponent;
import com.ractoc.fs.components.es.ControlledComponent;
import com.ractoc.fs.components.es.Controls;
import com.ractoc.fs.components.es.DamageComponent;
import com.ractoc.fs.components.es.HasFocusComponent;
import com.ractoc.fs.components.es.LocationComponent;
import com.ractoc.fs.components.es.MovementComponent;
import com.ractoc.fs.components.es.OriginComponent;
import com.ractoc.fs.components.es.RenderComponent;
import com.ractoc.fs.components.es.ShootMainComponent;
import com.ractoc.fs.components.es.SpeedComponent;
import com.ractoc.fs.components.es.StructureComponent;
import com.ractoc.fs.es.Entities;
import com.ractoc.fs.es.Entity;
import com.ractoc.fs.es.EntityComponent;
import com.ractoc.fs.es.componentstorages.InMemoryComponentStorage;
import com.ractoc.fs.parsers.ParserException;
import com.ractoc.fs.parsers.ai.AiScriptLoader;
import com.ractoc.fs.parsers.entitytemplate.EntityTemplate;
import com.ractoc.fs.parsers.entitytemplate.TemplateLoader;

public class Main extends SimpleApplication {

    private static Entities entities = Entities.getInstance();

    public Main() {
        super((AppState) null);
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        rootNode.addLight(new DirectionalLight());

        setupKeys();
        setupEntitySystem();
        setupAppStates();
        setupCamera();
        setupStarField();
        spawnPlayer();
    }

    private void setupStarField() {
        StarFieldAppState sfas = new StarFieldAppState(entities);
        sfas.setWidth(settings.getWidth());
        sfas.setHeight(settings.getHeight());
        sfas.setDensity(150);
        sfas.setNrLayers(3);
        sfas.setLayerBaseSpeed(0.01f);
        sfas.setLayerBaseSize(3);
        sfas.setStarFieldDistance(600);
        sfas.setRandomStarColorInterval(5);
        sfas.setRandomStarSizeInterval(5);
        sfas.setRandomStarSizeShift(5);
        sfas.setVisibility(0.75f);
        stateManager.attach(sfas);
    }

    private void setupCamera() {
        getCamera().setLocation(new Vector3f(0, 60, 0));
        getCamera().lookAt(Vector3f.ZERO, Vector3f.UNIT_Z);
    }

    private void spawnPlayer() {
        EntityTemplate template = (EntityTemplate) assetManager.loadAsset("/Templates/Entity/BasicShipTemplate.etpl");

        if (template.getComponents() != null && template.getComponents().size() > 0) {
            EntityComponent[] components = (EntityComponent[]) template.getComponentsAsArray();
            Entity entity = entities.createEntity(components);
            entities.addComponentsToEntity(entity, new LocationComponent(Vector3f.ZERO, new Quaternion(), new Vector3f(1, 1, 1)), new ControlledComponent(), new BoundedEntityComponent());
        } else {
            throw new ParserException("No components for template /Templates/Entity/BasicShipTemplate.etpl");
        }
    }

    private void setupEntitySystem() {
        TemplateLoader.setClassLoader(this.getClass().getClassLoader());
        assetManager.registerLoader(TemplateLoader.class, "etpl", "ETPL");
        assetManager.registerLoader(AiScriptLoader.class, "ais", "AIS");

        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          AiComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          BoundedEntityComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          CanMoveComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          ControlledComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          DamageComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          HasFocusComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          LocationComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          MovementComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          RenderComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          ShootMainComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          SpeedComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          StructureComponent.class);
        Entities.getInstance().registerComponentTypesWithComponentStorage(new InMemoryComponentStorage(),
                                                                          OriginComponent.class);
    }

    private void setupAppStates() {
        SceneAppState sceneAppState = new SceneAppState("Scenes/TriggerTest.j3o");
        sceneAppState.setPlayerCentric(false);
        stateManager.attach(sceneAppState);
        FlightControlAppState flightControlAppState = new FlightControlAppState();
        stateManager.attach(flightControlAppState);
        FlightAppState flightAppState = new FlightAppState();
        flightAppState.setBounded(true);
        stateManager.attach(flightAppState);
        AiAppState triggerAppState = new AiAppState();
        stateManager.attach(triggerAppState);
        stateManager.attach(new ShootingAppState());
        stateManager.attach(new DamageAppState());
    }

    private void setupKeys() {
        inputManager.addMapping(Controls.MOVE_FORWARD.name(),
                                new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(Controls.MOVE_BACKWARDS.name(),
                                new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(Controls.STRAFE_LEFT.name(),
                                new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping(Controls.STRAFE_RIGHT.name(),
                                new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping(Controls.ROTATE_LEFT.name(),
                                new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(Controls.ROTATE_RIGHT.name(),
                                new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping(Controls.SHOOT_MAIN.name(),
                                new KeyTrigger(KeyInput.KEY_SPACE));
    }
}
