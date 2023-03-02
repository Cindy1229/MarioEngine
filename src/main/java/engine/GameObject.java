package engine;

import java.util.ArrayList;
import java.util.List;

public class GameObject {
    private String name;
    private List<Component> components;

    public Transform transform;

    public GameObject(String name) {
        this.name = name;
        this.components = new ArrayList<>();
        this.transform = new Transform();
    }

    public GameObject(String name, Transform transform) {
        this.name = name;
        this.components = new ArrayList<>();
        this.transform = transform;
    }

    /**
     * Get the component object specified from gameobject's components
     * @param componentClass
     * @return
     * @param <T>
     */
    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (Component c : components) {
            if (componentClass.isAssignableFrom(c.getClass())) {
                try {
                    return componentClass.cast(c);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    assert false : "Error: can not cast component";
                }
            }
        }
        return null;
    }

    /**
     * Remove a component from game object - thread safe
     * @param componentClass
     * @param <T>
     */
    public <T extends Component> void removeComponent(Class<T> componentClass) {
        for (int i = 0; i< components.size();i++) {
            Component c = components.get(i);
            if (componentClass.isAssignableFrom(c.getClass())) {
                components.remove(i);
                return;
            }
        }
    }

    /**
     * Adds a component
     * @param c
     * @param <T>
     */
    public <T extends Component> void addComponent(Component c) {
        this.components.add(c);
        c.gameObject = this;
    }

    /**
     * Update all components
     * @param dt
     */
    public void update(float dt) {
        for (Component component : components) {
            component.update(dt);
        }
    }

    /**
     * Start all components
     */
    public void start() {
        for (Component c : components) {
            c.start();
        }
    }

}
