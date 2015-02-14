/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.itemRendering.systems;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.itemRendering.components.AnimateRotationComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Rotation;
import org.terasology.math.geom.BaseQuat4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.registry.In;

/**
 * Continuously rotates all entities in a location with an AnimateRotationComponent.
 * <p/>
 * Using synchronization will allow all rotated objects at the same speed to always be in the same rotation as each other.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class AnimateRotationClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    Time time;
    @In
    EntityManager entityManager;

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(AnimateRotationComponent.class, LocationComponent.class)) {
            AnimateRotationComponent animateRotationComponent = entity.getComponent(AnimateRotationComponent.class);
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);

            if (animateRotationComponent.isSynchronized) {
                float percentThroughRotation = (time.getGameTime() % (1 / animateRotationComponent.speed * 4)) / (1 / animateRotationComponent.speed * 4);

                Rotation rotation = Rotation.rotate(animateRotationComponent.yaw, animateRotationComponent.pitch, animateRotationComponent.roll);
                // assumes only 90 degree rotations
                Quat4f rotationDirection = new Quat4f(rotation.getQuat4f().getAxis(), rotation.getQuat4f().getAngle() * 4f * percentThroughRotation);

                locationComponent.setLocalRotation(rotationDirection);
            } else {
                Rotation rotation = Rotation.rotate(animateRotationComponent.yaw, animateRotationComponent.pitch, animateRotationComponent.roll);
                Quat4f rotationDirection = rotation.getQuat4f();
                Quat4f zero = new Quat4f(0, 0, 0, 1f);
                Quat4f rotationAmount = BaseQuat4f.interpolate(zero, rotationDirection, delta * animateRotationComponent.speed);

                Quat4f currentRotation = locationComponent.getLocalRotation();
                currentRotation.mul(rotationAmount);
                currentRotation.normalize();
                locationComponent.setLocalRotation(currentRotation);
            }
            entity.saveComponent(locationComponent);
        }
    }
}
