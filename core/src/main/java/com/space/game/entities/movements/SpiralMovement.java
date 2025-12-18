package com.space.game.entities.movements;

import com.badlogic.gdx.math.MathUtils;
import com.space.game.entities.Alien;
import com.space.game.entities.Spaceship;
import com.space.game.SpaceGame;

public class SpiralMovement implements MovementStrategy {
  private float elapsedTime;
  private float radiusDecay;
  private float angleSpeed;
  private float angleOffset;

  public SpiralMovement(float speed) {
    this.radiusDecay = speed;
    this.angleSpeed = SpaceGame.getGame().getWorldWidth() / 3840; // Approx 0.5 rad/s if width=1920

    // Start out of screen logic moved to move() or init
    // Original logic initialized startRadius dynamically? No, it calculated it in
    // update loop
    // strictly speaking `startRadius` in original code was local var in
    // `moveInSpiral`.
    // Wait, original `moveInSpiral` re-calculated startRadius EVERY FRAME?
    // "float startRadius = (float) Math.sqrt(...) + 50f;"
    // "float radius = Math.max(2, startRadius - radiusDecay * elapsedTime * speed *
    // deltaTime);"
    // The original logic seems suspect if `startRadius` is constant.
    // Let's look at `Alien.java` original:
    // float startRadius = ... (constant based on screen size)
    // float radius = Math.max(2, startRadius - radiusDecay * elapsedTime * speed *
    // deltaTime);
    // NO. Wait. `radiusDecay` IS `speed` in constructor: `radiusDecay = speed;`
    // And calculation is: `startRadius - radiusDecay * elapsedTime * speed *
    // deltaTime`?
    // Original: `startRadius - radiusDecay * elapsedTime * speed * deltaTime`
    // If `radiusDecay` == `speed`, then it's `start - speed * elapsedTime * speed *
    // dt`?
    // That seems dimensionally wrong or very fast acceleration.
    // Let's re-read the original file carefully.

    // Original Alien.java:
    // radiusDecay = speed;
    // In moveInSpiral:
    // float radius = Math.max(2, startRadius - radiusDecay * elapsedTime * speed *
    // deltaTime);
    // Wait, `speed` in that formula?
    // It says: `startRadius - radiusDecay * elapsedTime` -> That would be `start -
    // V * t`.
    // But original code: `startRadius - radiusDecay * elapsedTime * speed *
    // deltaTime`??
    // Maybe it meant: `radius -= speed * deltaTime` per frame?
    // Ah, `elapsedTime` accumulates `deltaTime`.
    // So `radius(t) = R0 - V * t`.
    // If the original formula was `start - speed * elapsedTime`, that is linear
    // decay.
    // But the original code writes: `startRadius - radiusDecay * elapsedTime *
    // speed * deltaTime`.
    // If `deltaTime` is small (0.016), it effectively scales down the decay
    // massively?
    // Or is it a mistake in original code?
    // "elapsedTime += deltaTime"
    // "radius = start - speed * elapsedTime" IS correct for linear approach.
    // But it multiplies by `speed * deltaTime` AGAIN?
    // Maybe `radiusDecay` isn't speed?
    // In constructor: `radiusDecay = speed`.
    // So it is `start - speed * elapsedTime * speed * dt`.
    // This effectively means `radius(t) = R0 - V * t * (V * dt)`.
    // Since `dt` varies small amount, this `V * dt` is roughly constant step size.
    // This is weird logic.
    // However, I must PRESREVE BEHAVIOR.
    // So I will copy the logic exactly.

    this.angleOffset = MathUtils.random(0f, MathUtils.PI2);
    this.elapsedTime = 0;
  }

  @Override
  public void move(Alien alien, Spaceship spaceship, float deltaTime) {
    float naveCenterX = spaceship.getPosition().x + spaceship.getBounds().width * spaceship.getScale() / 2;
    float naveCenterY = spaceship.getPosition().y + spaceship.getBounds().height * spaceship.getScale() / 2;

    elapsedTime += deltaTime;

    // Update radius decay if speed changes (Alien speed increases over time in
    // original update)
    // In original `Alien.update`: `speed += (deltaTime * speed / ...)`
    // `radiusDecay` was set ONCE in constructor to initial speed.
    // But `moveInSpiral` used `radiusDecay` AND `speed` (current speed).
    // Let's preserve that.

    float w = SpaceGame.getGame().getWorldWidth();
    float h = SpaceGame.getGame().getWorldHeight();
    float startRadius = (float) Math.sqrt((w / 2) * (w / 2) + (h / 2) * (h / 2)) + 50f;

    // Replicating exact formula:
    // radiusDecay (initial speed) * elapsedTime * currentSpeed * deltaTime
    // This looks broken in original but I must copy it "moveInSpiral" logic.
    // Wait, `radiusDecay` field in Alien was initialized to `speed`.
    // Was it final? No.
    // But `speed` changes in `update`. `radiusDecay` does not.

    float currentSpeed = alien.getSpeed();

    // Original: radiusDecay * elapsedTime * speed * deltaTime
    // Note: In my Strategy, I don't have access to `radiusDecay` field of Alien
    // unless I store it.
    // I stored it in constructor of Strategy.

    float radius = Math.max(2, startRadius - radiusDecay * elapsedTime * currentSpeed * deltaTime);

    // Wait, if `deltaTime` is part of the expression, it means the radius depends
    // on instantaneous framerate for the TOTAL position?
    // `elapsedTime` grows. `deltaTime` fluctuates.
    // So if frames are slow, radius is smaller? That results in JITTER.
    // It's undoubtedly a bug in original code, but "Cuidado pra nao quebrar o que
    // ja funciona".
    // If I fix it to `start - speed * elapsedTime`, I might change the speed of
    // approach significantly.
    // `speed * deltaTime` is `step_distance`.
    // `radiusDecay * elapsedTime` has units `Distance`.
    // So `Distance * Distance`? No.
    // Usage: `startRadius - (radiusDecay * elapsedTime) * (speed * deltaTime)`.
    // If speed=100, dt=0.016 -> speed*dt = 1.6.
    // radiusDecay=100.
    // Term = 100 * t * 1.6 = 160 * t.
    // So it approaches at 160 pixels/sec roughly?
    // If I change it, I must be careful.
    // I will keep it exactly.

    float angle = angleSpeed * elapsedTime + angleOffset;

    alien.getPosition().x = naveCenterX + radius * (float) Math.cos(angle);
    alien.getPosition().y = naveCenterY + radius * (float) Math.sin(angle);
  }
}
