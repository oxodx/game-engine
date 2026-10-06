package nl.oxod.gameengine.engine;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class GameObject {
  private final String name;
  private final List<Component> components;
  public Transform transform;
  @Getter
  @Setter
  private int zIndex;

  public GameObject(String name) {
    this(name, new Transform(), 0);
  }

  public GameObject(String name, Transform transform, int zIndex) {
    this.name = name;
    this.components = new ArrayList<>();
    this.transform = transform;
    this.zIndex = zIndex;
  }

  public <T extends Component> T getComponent(Class<T> componentClass) {
    for (Component c : components) {
      if (componentClass.isAssignableFrom(c.getClass())) {
        return componentClass.cast(c);
      }
    }

    return null;
  }

  public <T extends Component> void removeComponent(Class<T> componentClass) {
    for (int i = 0; i < components.size(); i++) {
      Component c = components.get(i);
      if (componentClass.isAssignableFrom((c.getClass()))) {
        components.remove(i);
        return;
      }
    }
  }

  public void addComponent(Component c) {
    this.components.add(c);
    c.gameObject = this;
  }

  public void update(float dt) {
    for (Component component : components) {
      component.update(dt);
    }
  }

  public void start() {
    for (Component component : components) {
      component.start();
    }
  }
}
