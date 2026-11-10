package com.forgotten.dimensions.client.render;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.content.ArmorSetDef;
import com.forgotten.dimensions.content.FdRarity;
import com.forgotten.dimensions.registry.ModItems;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Pièces en relief des armures du mod portées par les humanoïdes (joueurs, supports d'armure,
 * zombies...) : crête et visière du casque, plastron bombé, épaulières, ceinture, genouillères,
 * bottes renforcées. Chaque pièce suit l'os correspondant du modèle (tête, corps, bras, jambes).
 */
public class ArmorDetailsFeatureRenderer<S extends BipedEntityRenderState, M extends BipedEntityModel<S>> extends FeatureRenderer<S, M> {
    private final ModelPart root;

    public ArmorDetailsFeatureRenderer(FeatureRendererContext<S, M> context, ModelPart root) {
        super(context);
        this.root = root;
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, S state, float limbAngle, float limbDistance) {
        M model = getContextModel();
        ArmorSetDef head = set(state.equippedHeadStack), chest = set(state.equippedChestStack);
        ArmorSetDef legs = set(state.equippedLegsStack), feet = set(state.equippedFeetStack);
        if (head != null) {
            if (head.rarity().ordinal() >= FdRarity.EPIC.ordinal()) part(matrices, queue, light, model.head, "crest", head);
            part(matrices, queue, light, model.head, "visor", head);
            part(matrices, queue, light, model.head, "brow", head);
        }
        if (chest != null) {
            part(matrices, queue, light, model.body, "chest_plate", chest);
            part(matrices, queue, light, model.rightArm, "shoulder_r", chest);
            part(matrices, queue, light, model.leftArm, "shoulder_l", chest);
        }
        if (legs != null) {
            part(matrices, queue, light, model.body, "belt", legs);
            part(matrices, queue, light, model.rightLeg, "knee_r", legs);
            part(matrices, queue, light, model.leftLeg, "knee_l", legs);
        }
        if (feet != null) {
            part(matrices, queue, light, model.rightLeg, "boot_r", feet);
            part(matrices, queue, light, model.leftLeg, "boot_l", feet);
        }
    }

    private static ArmorSetDef set(ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : ModItems.armorSetOf(stack.getItem());
    }

    private void part(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, ModelPart bone, String name, ArmorSetDef set) {
        if (!bone.visible) return;
        Identifier texture = ForgottenDimensions.id("textures/entity/armor_detail/" + set.id() + ".png");
        RenderLayer layer = RenderLayers.armorCutoutNoCull(texture);
        matrices.push();
        bone.applyTransform(matrices);
        queue.submitModelPart(root.getChild(name), matrices, layer, light, OverlayTexture.DEFAULT_UV, null);
        matrices.pop();
    }
}
