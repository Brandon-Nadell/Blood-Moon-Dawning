package com.mygdx.game.item.weapon;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.*;
import com.mygdx.game.Realms.Dir;
import com.mygdx.game.TintedTexture.Model;
import com.mygdx.game.effects.SwordAnimation;
import com.mygdx.game.item.gear.*;

public class Sword extends CombatGear implements Weaponizable {
	
	private boolean axe;
	private boolean deflective;
	private boolean stab;
	
	public Sword(String name, Statistics statistics, Items texture, boolean axe, boolean deflective) {
		super(Type.WEAPON, name, texture == null ? null : texture.texture, statistics, texture);
		this.axe = axe;
		this.deflective = deflective;
		if (axe) {
			setInfo("This weapon can break some blocks");
		}
		if (deflective) {
			setInfo("This weapon can deflect projectiles");
		}
		setSound(Audio.Sounds.SWORD_HIT, 1f, Realms.vary(1f, 0.25f));
	}
	
	public Sword(String name, Statistics statistics, Items texture) {
		this(name, statistics, texture, false, false);
	}
	
	public Sword(String name, Statistics statistics, Model model, boolean axe, boolean deflective) {
		this(name, statistics, (Items)null, axe, deflective);
		setTexture(new TintedTexture(model));
	}
	
	public Sword(String name, Statistics statistics, Model model) {
		this(name, statistics, model, false, false);
	}
	
	public void render() {
		super.render();
		if (keyPressed()) {
			use(Game.xDir(), Game.yDir());
		}
	}
	
	public void use(float xDir, float yDir) {
		if (canUse() && testHandicap(1)) {
			resetCooldown();
			stats().setDir(xDir, yDir);
			Powerball pb = getOwner().get(Powerball.class);
			Realms.getGame().getRoom().addEffect(new SwordAnimation(this, Dir.VECTOR.to(Dir.ANGLE, Realms.roundVector(new Vector2(xDir, yDir)), Integer.class), pb != null && pb.powerUp() ? pb.getDamage() : 0, SwordAnimation.DURATION_DEFAULT + (int)getOwner().getBadgeValue(Badge.Type.LEVERAGE)));
			stats().applySpecialUse(getOwner());
			if (xDir % 1 == 0 && yDir % 1 == 0) {
				getOwner().setDirection(Realms.dirToIndex((int)xDir, (int)yDir));
			}
			Audio.play(Audio.Sounds.SWORD_SWING, 1f, Realms.vary(1f, 0.5f), getOwner().centerX());
		}
	}
	
	public boolean getAxe() {
		return axe;
	}
	
	public boolean getDeflective() {
		return deflective;
	}
	
	public boolean getStab() {
		return stab;
	}
	
	public Sword setStab(boolean stab) {
		this.stab = stab;
		return this;
	}
	
}
