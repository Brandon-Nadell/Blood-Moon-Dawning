package com.mygdx.game.item.weapon;

import com.mygdx.game.entity.*;
import com.mygdx.game.entity.MovingObject.MovingObjects;
import com.mygdx.game.*;
import com.mygdx.game.Audio.Sounds;
import com.mygdx.game.Room.Handicap.Check;
import com.mygdx.game.Statistics.StatType;
import com.mygdx.game.TintedTexture.Model;
import com.mygdx.game.item.gear.*;

public class Dagger extends CombatGear implements Weaponizable {
	
	private boolean passThrough;
	
	public Dagger(String name, Statistics statistics, Items texture, boolean passThrough) {
		super(Type.WEAPON, name, texture == null ? null : texture.texture, statistics, texture);
		this.passThrough = passThrough;
		setSizeMax(passThrough ? 8 : 24);
		if (passThrough) {
			setSmall(10);
			setLarge(80);
		} else {
			setSmall(26);
			setLarge(65);
		}
		if (getSpin()) {
			setSmall(50);
			setLarge(50);
		}
	}
	
	public Dagger(String name, Statistics statistics, Model model, boolean passThrough) {
		this(name, statistics, (Items)null, passThrough);
		setTexture(new TintedTexture(model));
		if (getSpin()) {
			setSmall(50);
			setLarge(50);
		}
	}

	public void render() {
		super.render();
		if (keyPressed()) {
			use(Game.xDir(), Game.yDir());
		}
	}
	
	public void use(float xDir, float yDir) {
		if (canUse() && testHandicap(Check.RANGE)) {
			createProjectile(this, xDir, yDir);
		}
	}
	
	public static void createProjectile(CombatGear item, float xDir, float yDir) {
		item.resetCooldown();
		item.unstack();
		Creature owner = item.getOwner();
		int width = (int)((xDir == 0 ? item.getSmall() : item.getLarge())*(1 + item.getOwner().getBadgeValue(Badge.Type.ENLARGEMENT)));
		int	height = (int)((yDir == 0 ? item.getSmall() : item.getLarge())*(1 + item.getOwner().getBadgeValue(Badge.Type.ENLARGEMENT)));
		double speed = item.stats().getStat(StatType.SPEED) + owner.getBadgeValue(Badge.Type.TWITCH);
		Infuser inf = item.getOwner().get(Infuser.class);
		if (inf != null) {
			item = item.clone();
			item.setOriginal(item.stats().clone());
			inf.fuse(item);
		}
		Projectile projectile = new Projectile(owner.getGroup(), 
			owner.centerX() - width/2 + (int)(xDir*owner.getWidth()),
			owner.centerY() - height/2 + (int)(yDir*owner.getWidth()), 
			width, height, speed*xDir, speed*yDir, MovingObjects.DAGGER, item);
		Realms.getGame().getRoom().addProjectile(projectile);
		Powerball pb = owner.get(Powerball.class);
		if (pb != null && pb.powerUp()) {
			projectile.setDamageBoost(pb.getDamage());
			projectile.getForceMain().setLength(projectile.getForceMain().len() + pb.getSpeed());
			projectile.setWidth((int)(projectile.getWidth()*1.5));
			projectile.setHeight((int)(projectile.getHeight()*1.5));
		}
		item.stats().applySpecialUse(owner);
		if (xDir % 1 == 0 && yDir % 1 == 0) {
			owner.setDirection(Realms.dirToIndex((int)xDir, (int)yDir));
		}
		Audio.play(Sounds.SWORD_SWING, 1f, Realms.vary(1.25f, 0.25f), owner.centerX());
	}
	
	
	
	public boolean getPassThrough() {
		return passThrough;
	}
	
}
