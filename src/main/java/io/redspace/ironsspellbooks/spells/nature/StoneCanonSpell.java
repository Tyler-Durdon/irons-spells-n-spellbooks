package io.redspace.ironsspellbooks.spells.nature;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.stone_lance.StoneLanceProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class StoneCanonSpell extends AbstractSpell {

    private static final int MAX_CHARGE_TIME = 60; // 3 seconds max charge
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "stone_canon");
    private final DefaultConfig defaultConfig = new DefaultConfig().setMinRarity(SpellRarity.UNCOMMON).setSchoolResource(SchoolRegistry.NATURE_RESOURCE).setMaxLevel(10).setCooldownSeconds(20).build();

    public StoneCanonSpell() {
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 2;
        this.castTime = 60;
        this.baseManaCost = 50;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    public int getMaxChargeTime() {
        return MAX_CHARGE_TIME;
    }

    public float getBaseDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster);
    }

    public float getMaxDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 3.0f; // 3x damage at max charge
    }

    public float getDamageForCharge(int spellLevel, LivingEntity caster, int chargeTime) {
        float chargeRatio = Math.min((float) chargeTime / MAX_CHARGE_TIME, 1.0f);
        float baseDamage = getBaseDamage(spellLevel, caster);
        float maxDamageBonus = getMaxDamage(spellLevel, caster) - baseDamage;

        // Apply non-linear scaling for more satisfying charge progression
        float chargeMultiplier = chargeRatio * chargeRatio; // Square for exponential feel

        return baseDamage + (maxDamageBonus * chargeMultiplier);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getBaseDamage(spellLevel, caster), 1)), Component.translatable("ui.irons_spellbooks.max_damage", Utils.stringTruncation(getMaxDamage(spellLevel, caster), 1)), Component.translatable("ui.irons_spellbooks.charge_time", Utils.stringTruncation(getMaxChargeTime() / 20.0, 1)));
    }

    // TODO CGN
    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.NATURE_CAST.get());
    }

    // TODO CGN
    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.GUST_CAST.get());
    }

    // TODO CGN
    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_CAST_ONE_HANDED;
    }

    // TODO CGN
    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.SLASH_ANIMATION;
    }

    // CHARGED SPELL
    //    @Override
    //    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
    //        // Debug logs
    //        IronsSpellbooks.LOGGER.debug("StoneCanonSpell onCast - Spell Level: {}, Cast Duration: {}",
    //                spellLevel, playerMagicData.getCastDuration());
    //
    //        // Get the charge time from the player's magic data
    //        int chargeTime = playerMagicData.getCastDuration();
    //
    //        // Calculate damage based on charge time
    //        float damage = getDamageForCharge(spellLevel, entity, chargeTime);
    //
    //        // Create and launch the stone lance projectile
    //        StoneLanceProjectile stoneLance = new StoneLanceProjectile(level, entity);
    //        stoneLance.setPos(entity.position().add(0, entity.getEyeHeight() - stoneLance.getBoundingBox().getYsize() * 0.5f, 0));
    //        stoneLance.shoot(entity.getLookAngle());
    //        stoneLance.setDamage(damage);
    //
    //        level.addFreshEntity(stoneLance);
    //
    //        // Play sound effect
    //        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
    //                SoundRegistry.POISON_ARROW_CAST.get(),
    //                entity.getSoundSource(), 1.0F, 1.0F);
    //
    //        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    //    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        // Debug logs
        IronsSpellbooks.LOGGER.debug("StoneCanonSpell onCast - Spell Level: {}, Cast Duration: {}", spellLevel, this.castTime);

        // For LONG cast type, we use the full cast time
        int chargeTime = this.castTime;

        // Calculate damage based on charge time (always max for LONG type)
        float damage = getMaxDamage(spellLevel, entity);

        // Create and launch the stone lance projectile
        StoneLanceProjectile stoneLance = new StoneLanceProjectile(level, entity);
        stoneLance.setPos(entity.position().add(0, entity.getEyeHeight() - stoneLance.getBoundingBox().getYsize() * 0.5f, 0).add(entity.getForward()));
        stoneLance.shoot(entity.getLookAngle());
        stoneLance.setDamage(damage);

        level.addFreshEntity(stoneLance);

        // Play sound effect
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.FIRE_ARROW_CAST.get(), entity.getSoundSource(), 1.0F, 1.0F);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}