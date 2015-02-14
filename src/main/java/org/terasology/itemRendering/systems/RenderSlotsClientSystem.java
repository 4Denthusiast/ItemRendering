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

import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.itemRendering.components.RenderInventorySlotsComponent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;

import java.util.List;

@RegisterSystem(RegisterMode.CLIENT)
public class RenderSlotsClientSystem extends RenderOwnedEntityClientSystemBase {

    @In
    InventoryManager inventoryManager;
    @In
    WorldProvider worldProvider;

    @ReceiveEvent
    public void addRemoveItemRendering(OnChangedComponent event,
                                       EntityRef inventoryEntity,
                                       InventoryComponent inventoryComponent) {
        refreshRenderedItems(inventoryEntity);
    }

    @ReceiveEvent
    public void initExistingItemRendering(OnActivatedComponent event,
                                          EntityRef inventoryEntity,
                                          InventoryComponent inventoryComponent) {
        refreshRenderedItems(inventoryEntity);
    }

    private void refreshRenderedItems(EntityRef inventoryEntity) {
        RenderInventorySlotsComponent renderInventoryInCategory = inventoryEntity.getComponent(RenderInventorySlotsComponent.class);

        List<Integer> slots = Lists.newArrayList();
        if (renderInventoryInCategory != null) {
            slots = renderInventoryInCategory.slots;
        }

        // ensure all non rendered inventory slots have been reset
        for (int slot = 0; slot < inventoryManager.getNumSlots(inventoryEntity); slot++) {
            EntityRef item = inventoryManager.getItemInSlot(inventoryEntity, slot);
            if (!slots.contains(slot)) {
                removeRenderingComponents(item);
            }
        }

        for (int slot : slots) {
            EntityRef item = inventoryManager.getItemInSlot(inventoryEntity, slot);
            addRenderingComponents(inventoryEntity, item, renderInventoryInCategory);
        }
    }
}