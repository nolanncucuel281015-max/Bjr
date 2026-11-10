package com.forgotten.dimensions.client.render;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Modèle générique animé selon les conventions de noms des parties : tête qui suit le regard,
 * jambes et bras qui balancent à la marche, bras levés à l'attaque, ailes qui battent, queues et
 * segments qui ondulent, tentacules, orbes en orbite, flottement des créatures volantes.
 */
public class FdCreatureModel extends EntityModel<FdCreatureRenderState> {
    private final ModelPart head;
    private final List<ModelPart> legs = new ArrayList<>();
    private final List<ModelPart> spiderLegsRight = new ArrayList<>();
    private final List<ModelPart> spiderLegsLeft = new ArrayList<>();
    private final List<ModelPart> arms = new ArrayList<>();
    private final List<ModelPart> wings = new ArrayList<>();
    private final List<ModelPart> chain = new ArrayList<>();
    private final List<ModelPart> tentacles = new ArrayList<>();
    private final List<ModelPart> orbs = new ArrayList<>();
    private final List<ModelPart> fins = new ArrayList<>();
    private final List<ModelPart> claws = new ArrayList<>();

    public FdCreatureModel(ModelPart root) {
        super(root);
        this.head = root.hasChild("head") ? root.getChild("head") : null;
        collect(root);
    }

    private void collect(ModelPart part) {
        for (String name : new String[]{"leg_r", "leg_l", "leg_fr", "leg_fl", "leg_br", "leg_bl"}) {
            if (part.hasChild(name)) legs.add(part.getChild(name));
        }
        for (int i = 0; i < 4; i++) {
            if (part.hasChild("leg_r" + i)) spiderLegsRight.add(part.getChild("leg_r" + i));
            if (part.hasChild("leg_l" + i)) spiderLegsLeft.add(part.getChild("leg_l" + i));
        }
        for (String name : new String[]{"arm_r", "arm_l"}) if (part.hasChild(name)) arms.add(part.getChild(name));
        for (String name : new String[]{"wing_r", "wing_l"}) if (part.hasChild(name)) wings.add(part.getChild(name));
        for (String name : new String[]{"fin_r", "fin_l", "fin_top", "tail"}) if (part.hasChild(name)) fins.add(part.getChild(name));
        for (String name : new String[]{"claw_r", "claw_l"}) if (part.hasChild(name)) claws.add(part.getChild(name));
        for (int i = 0; i < 8; i++) {
            if (part.hasChild("tentacle" + i)) tentacles.add(part.getChild("tentacle" + i));
            if (part.hasChild("orb" + i)) orbs.add(part.getChild("orb" + i));
        }
        // Chaînes (segments, queue) : chaque segment est l'enfant du précédent.
        ModelPart seg = part.hasChild("seg0") ? part.getChild("seg0") : part.hasChild("tail0") ? part.getChild("tail0") : null;
        String prefix = part.hasChild("seg0") ? "seg" : "tail";
        int i = 1;
        while (seg != null) {
            chain.add(seg);
            String next = prefix + i++;
            seg = seg.hasChild(next) ? seg.getChild(next) : null;
        }
    }

    @Override
    public void setAngles(FdCreatureRenderState s) {
        super.setAngles(s);
        float limb = s.limbSwingAnimationProgress;
        float amp = Math.min(1.0F, s.limbSwingAmplitude);
        float age = s.age;
        if (head != null) {
            head.yaw += s.relativeHeadYaw * MathHelper.RADIANS_PER_DEGREE;
            head.pitch += s.pitch * MathHelper.RADIANS_PER_DEGREE;
        }
        for (int i = 0; i < legs.size(); i++) {
            float phase = (i % 2 == 0) ? 0 : MathHelper.PI;
            if (legs.size() == 4 && i >= 2) phase += MathHelper.PI;
            legs.get(i).pitch += MathHelper.cos(limb * 0.6662F + phase) * 1.2F * amp;
        }
        for (int i = 0; i < spiderLegsRight.size(); i++) {
            float swing = MathHelper.cos(limb * 1.3F + i * MathHelper.HALF_PI) * 0.4F * amp;
            spiderLegsRight.get(i).yaw += swing;
            spiderLegsLeft.get(i).yaw -= swing;
        }
        for (int i = 0; i < arms.size(); i++) {
            ModelPart arm = arms.get(i);
            arm.pitch += MathHelper.cos(limb * 0.6662F + (i == 0 ? MathHelper.PI : 0)) * 0.9F * amp;
            arm.roll += (i == 0 ? 1 : -1) * (MathHelper.cos(age * 0.09F) * 0.05F + 0.05F);
            if (s.attackProgress > 0) arm.pitch -= MathHelper.sin(s.attackProgress * MathHelper.PI) * 1.6F;
        }
        float flap = MathHelper.sin(age * (s.flying ? 0.45F : 0.15F)) * (s.flying ? 0.7F : 0.15F);
        for (int i = 0; i < wings.size(); i++) wings.get(i).roll += (i == 0 ? -flap : flap);
        for (int i = 0; i < chain.size(); i++) {
            chain.get(i).yaw += MathHelper.sin(age * 0.25F - i * 0.7F) * 0.25F;
            if (s.flying || s.swimming) chain.get(i).pitch += MathHelper.sin(age * 0.2F - i * 0.6F) * 0.08F;
        }
        for (int i = 0; i < tentacles.size(); i++) tentacles.get(i).pitch += MathHelper.sin(age * 0.15F + i) * 0.25F;
        for (int i = 0; i < orbs.size(); i++) {
            float a = age * 0.08F + i * MathHelper.HALF_PI;
            ModelPart orb = orbs.get(i);
            orb.originX += MathHelper.cos(a) * 6.0F;
            orb.originZ += MathHelper.sin(a) * 6.0F;
            orb.originY += MathHelper.sin(age * 0.12F + i) * 1.5F;
        }
        for (int i = 0; i < fins.size(); i++) fins.get(i).yaw += MathHelper.sin(age * 0.3F + i) * 0.2F;
        for (int i = 0; i < claws.size(); i++) claws.get(i).pitch += MathHelper.sin(age * 0.2F + i * MathHelper.PI) * 0.15F;
        if (s.flying || (!orbs.isEmpty() || !tentacles.isEmpty())) {
            root.originY += MathHelper.sin(age * 0.1F) * 1.2F;
        }
    }
}
