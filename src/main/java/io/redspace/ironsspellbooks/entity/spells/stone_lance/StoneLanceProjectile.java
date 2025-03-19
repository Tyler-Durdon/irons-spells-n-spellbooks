package io.redspace.ironsspellbooks.entity.spells.stone_lance;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class StoneLanceProjectile extends AbstractMagicProjectile {
    private float damage;

    public StoneLanceProjectile(EntityType<? extends AbstractMagicProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public StoneLanceProjectile(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.STONE_LANCE_PROJECTILE.get(), levelIn);
        this.setOwner(shooter);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setScale(float scale) {
        // Update hitbox size based on scale
        this.refreshDimensions();
    }

    @Override
    public void trailParticles() {
        Vec3 motion = getDeltaMovement();
        double x = getX() - motion.x * 0.25;
        double y = getY() - motion.y * 0.25;
        double z = getZ() - motion.z * 0.25;

        // Add stone/earth particles
        level.addParticle(ParticleTypes.EFFECT, x, y, z, 0, 0, 0);
        level.addParticle(ParticleTypes.CRIT, x, y, z, 0, 0, 0);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        // Create an explosion of stone particles on impact
        for (int i = 0; i < 20; i++) {
            double offsetX = random.nextGaussian() * 0.2;
            double offsetY = random.nextGaussian() * 0.2;
            double offsetZ = random.nextGaussian() * 0.2;
            level.addParticle(ParticleTypes.EXPLOSION, x, y, z, offsetX, offsetY, offsetZ);
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Add debug logging
        if (this.tickCount == 1) {
            IronsSpellbooks.LOGGER.debug("StoneLanceProjectile spawned at: {}, with damage: {}", this.position(), this.damage);
        }

        // Make sure we're generating particles
        this.trailParticles();
    }

    protected void onHitEntity(LivingEntity target) {
        IronsSpellbooks.LOGGER.debug("StoneLanceProjectile hit entity: {}, applying damage: {}", target.getName().getString(), this.damage);

        target.hurt(this.damageSources().indirectMagic(this, getOwner()), damage);
    }

    @Override
    public float getSpeed() {
        return 1.5f;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.of(SoundEvents.GENERIC_EXPLODE);
    }

    public float getMaxDistance() {
        return 40;
    }
}
