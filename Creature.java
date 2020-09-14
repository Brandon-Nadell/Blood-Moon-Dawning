package com.mygdx.game.entity;

import java.awt.geom.Line2D;

public class Creature extends MovingObject {
	
	//specific
	private ArrayList<Item> items; //list of weapons
	private ArrayList<Loot> loot; //list of possible items to be dropped upon death
	private String name; //if != null, will display as notifications upon death
	private Intelligence ai;
	private Divisibility divisibility; //if/how creature will divide upon death
	private Creatures creatureType;
	//movement
	private double speedBase; //speed under restrictions (ex. dirt)
	private double speedReduction; //% reduction of speedBase
	private float freezeDuration; //time until becomes unfrozen
	//damage
	private ArrayList<DamageOverTime> dots; //DamageOverTime effects
	private double lootBoost; //stores the value the last weapon with loot SFX
	private Creature killer; //the last creature that damaged this
	private int hurtCooldown; //# of ticks until can be hurt
	private int hurtCooldownColor; //# of ticks until damage color wears off
	//drawing
	private HealthBar healthbar;
	private Color color; //tint
	private Color color2;
	private float a; //transparency
	private int direction; //0-down, 1-up, 2-left, 3-right
	private boolean rotates; //if true, faces player
	private int textureIndex; //index of the current direction
	//effects & options
	private ArrayList<Effect> effects;
	private boolean blind; //if true, surrounded by darkness
	private boolean dyslexic; //if true, letters are backwards
	private boolean clairvoyant; //if true, sees items in chests
	private boolean cloaked; //if true, 15% opaque
	private boolean hidden; //if true, 0% opaque
	private boolean disabled; //if true, cannot use weapons
	private boolean disablePickup; //if true, cannot pickup items
	private ModBlock nullZone; //area that deflects projectiles
	private int nullZoneDuration; //remaining time of nullZone
	private int zoneRotation; //remaining time of nullZone
	private boolean trueDamageResistant; //if true, does not take damage that ignores resistance (ex. DOTs)
	private boolean explosionResistant; //if true, does not take damage from bomb explosions
	private boolean noLoot; //if true, does not drop loot (excluding mini-hearts)
	private boolean noMiniHearts; //if true, does not drop mini-hearts
	private boolean canGoOffscreen; //if true, is not killed when offscreen
	public Creature tether;
	//other
	private Musics music; //boss soundtrack
	private int ID; //used for saving Creature references
	public static int IDcount; //total # of creatures to exist
	public static final int HURT_COOLDOWN = 40;
	
	public Creature(MovingObjects type, Texture texture, Group group, int x, int y, int width, int healthMax, double speedBase, Intelligence ai) {
		super(group, x, y, width, texture == null ? 0 : (int)(width*texture.getHeight()/texture.getWidth()), 0, 0, type, (Texture)null);
		this.speedBase = speedBase;
		items = new ArrayList<Item>();
		loot = new ArrayList<Loot>();
		dots = new ArrayList<DamageOverTime>();
		effects = new ArrayList<Effect>();
		healthbar = new HealthBar(healthMax, healthMax, type.display);
		setAI(ai);
		color = color2 = Color.WHITE;
		a = 1f;
		
		ID = IDcount;
		IDcount++;
		
		for (int i = 0; i < type.texture.length; i++) {
			if (type.texture[i] == texture) {
				this.textureIndex = i;
			}
		}
		setSprite(type.textures[textureIndex][0]);
	}
	
	public Creature(MovingObjects type, Group group, int x, int y, int width, int healthMax, double speedBase, Intelligence ai) {
		this(type, type.texture[(int)(Realms.random()*type.texture.length)], group, x, y, width, healthMax, speedBase, ai);
	}
	
	public Creature(MovingObjects type, Group group, int x, int y, int width, int healthMax, double speed, Intelligence ai, Loot loot) {
		this(type, group, x, y, width, healthMax, speed, ai);
		this.loot.add(loot);
	}
	
	//used only from 5th constructor
	private Creature(Creatures c, Group group, int x, int y, int width, double speedBase, Intelligence ai) {
		this(c.types[(int)(Realms.random()*c.types.length)], group, x, y, width, c.healthMin*width/c.widthMin, speedBase, ai);
	}
	
	public Creature(Creatures c, int x, int y) {
		this(c, c.wild ? Group.NEUTRAL : Group.FOE, x, y, Realms.random(c.widthMin, c.widthMax), c.speed, c.ai == null ? null : c.ai.clone());
		setCenterX(x);
		setCenterY(y); 
		for (int i = 0; i < c.items.length; i++) {
			addItem(c.items[i].clone());
		}
		this.creatureType = c;
		loot.addAll(Arrays.asList(c.loot));
		if (c.ai != null) {
			divisibility = c.ai.getDivisibility();
		}
	}
	
	public static ArrayList<Creature> spawn(Creatures c, int x, int y, Room room, int size, int variance) {
		ArrayList<Creature> spawn = new ArrayList<Creature>();
		while (size > 0) {
			//create creature of random variation from pool
			Creature creature = new Creature(c.variations == null ? c : c.variant(), x, y);
			//ensure is not touching any solids
			ArrayList<Entity> entities = room.getEntities(true, true, false, true, true);
			int attempts = 0;
			while (creature.touching(entities)) {
				creature.setCenterX(Realms.limit(Realms.vary(x, variance), creature.getWidth()/2, room.getXMax() - creature.getWidth()/2));
				creature.setCenterY(Realms.limit(Realms.vary(y, variance), creature.getHeight()/2, room.getYMax() - creature.getHeight()/2));
				if (++attempts > 100) {
					variance += 50;
				}
			}
			room.getCreatures().add(creature);
			spawn.add(creature);
			size--;
		}
		return spawn;
	}
	
	public static ArrayList<Creature> spawn(Creatures c, int x, int y, Room room) {
		return spawn(c, x, y, room, Realms.random(c.sizeMin, c.sizeMax), 500);
	}
	
	//methods
	public void render(boolean frozen) {
		if (getLife() == 0 && creatureType != null) {
			creatureType.create(this);
		}
		if (ai != null) {
			ai.render(frozen);
		}
		if (!frozen) {
			move();
			testCollisions(frozen);
			if (get(RustedGold.class) != null || get(Magnet.class) != null || nullZone != null) {
				zoneRotation++;
			}
			if (nullZone != null) {
				nullZone.setCenterX(centerX());
				nullZone.setCenterY(centerY());
				nullZone.setRotation(zoneRotation);
				if (--nullZoneDuration == 0) {
					Realms.getGame().getRoom().getBlocks().remove(nullZone);
					nullZone = null;
				}
			}
			updateDOTs();
			if (creatureType != null) {
				creatureType.special(this);
			}
			if (this instanceof Player) {
				Realms.getGame().getPlayer().renderDoors();
			}
			if (hurtCooldown > 0) {
				hurtCooldown--;
			}
			if (hurtCooldownColor > 0) {
				hurtCooldownColor--;
			}
			freezeDuration = Math.max(freezeDuration -= getGameSpeed(), -1);
			if (get(ChestToy.class) == null) {
				hidden = false;
			}
			for (Item item : items) {
				if (item != null) {
					item.render();
				}
			}
			if (this instanceof Player) {
				((Player)this).getHungerbar().update();
			}
			//item picking-up && magnets
			if (!disablePickup) {
				Magnet magnet = get(Magnet.class);
				for (Item item : Realms.getGame().getRoom().getItems()) {
					if ((getRustedGold(item) || touching(item)) && item.getPickupDelay() <= 0 && !item.shouldDelete()) {
						item.pickupBy(this);
					}
					if (magnet != null) {
						magnet.magnetize(item, this);
					}
				}
			}
			if (rotates && freezeDuration <= 0) {
				rotateToFace(Realms.getGame().getPlayer());
			}
		}
		if (healthbar.getDisplay() == Display.DEFAULT) {
			for (int i = 0; i < items.size(); i++) {
				items.get(i).draw(centerX() + 40 + i*25, getY2() + 6, 0, 0, 20, 20, true, false, a);
			}
			if (creatureType != null) {
				Text word = new Text(Realms.capitalize(creatureType.toString()), 0, 0, getType().color.cpy().mul(1, 1, 1, a), 12, Format.GRADIENT);
				word.render(true, centerX() - word.getWidth()/2, getY2() + 30);
			}
		}
		super.render();
	}
	
	public void rotateToFace(Creature target) {
		setRotation((int)Math.toDegrees(Realms.arctan(target.centerX() - centerX(), target.centerY() - centerY())));
	}
	
	public void testCollisions(boolean frozen) {
		//doors
		Door entering = null;
		for (Door door : Realms.getGame().getRoom().getDoors()) {
			if (this instanceof Player && entering(door) && Room.canExit()) {
				entering = door;
			}
		}
		if (entering != null) {
			Realms.getGame().getPlayer().enterDoor(entering);
		}
		//non-solid effects
		for (Block block : Realms.getGame().getRoom().getBlocks()) {
			if (!frozen && !block.getSolid() && touching(block)) {
				if (block instanceof Chest && this instanceof Player) {
					((Chest)block).open();
				} else if (block instanceof Floor/* && ((Floor)block).touching(this)*//*!(block instanceof FloorRing && fullyContainedIn(((FloorRing)block).getBounds()))*/) { 
					((Floor)block).effect(this);
				}
			}
		}
		//creature
		if (!getPermeable()) {
			for (Creature creature : Realms.getGame().getRoom().getCreatures2()) {
				if (entering(creature) && creature != this && !creature.getPermeable()) {
					collide(creature);
				}
			}
			unCollideAll();
		}
	}
	
	public void updateDOTs() {
		for (int i = 0; i < dots.size(); i++) {
			if (dots.get(i).intervalPassed()) {
				color = dots.get(i).effect(this);
			}
			if (dots.get(i).update(this)) {
				dots.remove(i);
				i--;
			}
		}
		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).render(this)) {
				effects.get(i).effectEnd(this);
				effects.remove(i);
				i--;
			}
		}
	}
	
	public void collide(Creature target) {
		unCollide(target);
		if (!target.getImmovable()) {
			target.addForce(sumVelX(), sumVelY());
			addForce(-sumVelX(), -sumVelY());
		} else {
			Rectangle2D collision = target.toRect().createIntersection(toRect());
			boolean tOb = collision.getWidth() > collision.getHeight();
			Line2D.Float left = new Line2D.Float(target.getX2(), target.getY(), target.getX2(), target.getY2());
			Line2D.Float right = new Line2D.Float(target.getX(), target.getY(), target.getX(), target.getY2());
			if (!tOb && (left.intersects(toRect()) || right.intersects(toRect()))) {
				addForce(Math.signum(-sumVelX())*3.5, 0);
			}
			Line2D.Float up = new Line2D.Float(target.getX(), target.getY2(), target.getX2(), target.getY2());
			Line2D.Float down = new Line2D.Float(target.getX(), target.getY(), target.getX2(), target.getY());
			if (tOb && (up.intersects(toRect()) || down.intersects(toRect()))) {
				addForce(0, Math.signum(-sumVelY())*3.5);
			}
		}
		SpikeHelmet helm = getSpikeHelmet();
		if (helm != null) {
			damage(helm, target, false, 0);
			helm.hit(target);
		}
		helm = target.getSpikeHelmet();
		if (helm != null) {
			target.damage(helm, this, false, 0);
			helm.hit(this);
		}
	}
	
	public void draw() {
		Graphics.setColor(1f, 1f, 1f, totalA(), true);
		RustedGold gold = get(RustedGold.class);
		if (gold != null) {
			int r = gold.getRadius();
			Graphics.begin();
			Graphics.draw(new TextureRegion(ScreenEffects.GOLDZONE.texture), centerX() - r, centerY() - r, r, r, r*2, r*2, 1, 1, zoneRotation);
			Graphics.end();
		}
		Magnet magnet = get(Magnet.class);
		if (magnet != null) {
			int r = magnet.getRadius();
			Graphics.begin();
			Graphics.draw(new TextureRegion(ScreenEffects.MAGNETZONE.texture), centerX() - r, centerY() - r, r, r, r*2, r*2, 1, 1, zoneRotation);
			Graphics.end();
		}
		Graphics.resetColor();
		Color c = color2.equals(Color.WHITE) ? color.cpy().lerp(color2.cpy(), .5f) : color.cpy().lerp(color2.cpy(), .85f);
		Graphics.setColor(c.r, c.g, c.b, c.a*totalA(), true);
		if (hidden) {
			Graphics.begin();
			Graphics.draw(new TextureRegion(new Texture("entity/creature/chest.png")), getX(), getY(), getWidth(), getHeight());
			Graphics.end();
			drawBounds();
		} else {
			super.draw();
		}
		if (hurtCooldownColor == 0) {
			color = Color.WHITE;
		}
		if (freezeDuration > 0) {
			Graphics.begin();
			Graphics.draw(Realms.toRegion(ScreenEffects.ICE.texture), getX() - 10, getY() - 10, getWidth()/2, getHeight()/2, getWidth() + 20, getHeight() + 20, 1, 1, 0);
			Graphics.end();
		}
		if (freezeDuration <= 0 && freezeDuration > -1) {
			unFreeze();
			freezeDuration = -1;
		}
		Graphics.resetColor();
	}
	
	public void drawHealthBar(boolean frozen) {
		healthbar.render(this, frozen, new Color(1f, 1f, 1f, a));
		Graphics.setColor(new Color(1f, 1f, 1f, a));
	}
	
	public void move() {
		updateSpeed();
		move(freezeDuration, friction());
		if (this instanceof Player) {
			((Player)this).moveByKeys();
		}
	}
	
	public float friction() {
		//returns speedReduction of floor
		for (Block block : Realms.getGame().getRoom().getBlocks()) {
			if (block instanceof Floor && !block.getSolid() && ((Floor)block).getEffect() == Floor.FloorEffect.ICE && touching(block) && !getBootIce()) {
				return (float)((Floor)block).getValue();
			}
		}
		return Game.FRICTION;
	}
	
	public boolean offscreenCompletely() {
		return !canGoOffscreen && super.offscreenCompletely();
	}
		
	public static void lifesteal(Creature attacker, Creature victim, int health) {
		attacker.changeHealth(health);
		Realms.getGame().getRoom().addEffect(new Lifesteal(victim, attacker, Color.RED, 20));
		Audio.play(Sounds.LIFESTEAL, attacker.centerX());
	}
	
	//normal damage
	public void damage(CombatGear item, Creature victim, boolean force, int damageBoost) {
		damage(item, getGroup(), victim, force, 0, Realms.arctan(victim.centerX() - centerX(), victim.centerY() - centerY()), true, damageBoost);
	}
	
	public void damage(CombatGear item, Group group, Creature victim, boolean force, int distance, double angleFromSource, boolean sourceIsCreature, int damageBoost) {
		Statistics stats = item.stats();
		if (victim.getGroup() != group) {
			double damage = damageBoost;
			boolean crit = stats.crit(getBadgeValue(Badge.Type.LUCK));
			if ((victim.canHurt() || force) && victim.hurtCooldown != -1) {
				double damageWithCrit = stats.getCritDamage(crit, victim.getCritResistance(), 0, damageBoost);
				double resistance = victim.getResistance(stats) + victim.getActiveResistance(stats);
				damage = -damageWithCrit * (1 - Realms.limit(resistance - (!stats.getImmutable(StatType.POTENCY) ? stats.getStat(StatType.POTENCY) : 0), 0, 1));
				damage *= (1 - compareEssence(item.getEssences(), victim.getEssences()));
				Shields shieldType = victim.getShieldType();
				if (shieldType != null) {
					shieldType.action(victim, -damageWithCrit + damageWithCrit * (1.0 - resistance));
				}
				if (stats.getAttribute(Special.LOOT) != null) {
					victim.setLootBoost(stats.getAttribute(Special.LOOT).getValue());
				}
				damage = victim.modifyDamage(item, damage);
				victim.changeHealth((int)(damage*(Realms.random() < victim.getEnchantedFangChance() ? -1 : 1)));
				if (victim.getHealthbar().getHealth() > 0) {
					if ((int)damage != 0) {
						victim.hurt(!force, Color.RED);
					}
				} else {
					victim.setShouldDelete(true);
					Realms.getGame().getRoom().addEffects(Particle.create(Type.DEATH, victim.centerX(), victim.centerY(), 3, 3, 20, victim.getWidth(), false, true));
				}
				if (resistance > 0) {
					Audio.play(Sounds.SHIELD, 0.5f, 0.75f, victim.centerX());
				}
				Realms.getGame().getRoom().addEffect(Particle.create(new Text(Integer.toString((int)damage), crit ? Color.GOLD : Color.RED, Format.GRADIENT), victim.centerX(), victim.centerY(), 5, 5, 40));
				victim.setKiller(this);
				victim.setColor(DamageOverTime.DAMAGE);
				if (crit) {
					Radialith radialith = get(Radialith.class);
					if (radialith != null) {
						radialith.shock();
					}
					Audio.play(Sounds.REFLECT, 1f, 2f, victim.centerX());
				}
			}
			stats.applySpecialHit(this, victim, (int)damage, crit, distance, angleFromSource, sourceIsCreature);
		}
	}
	
	//true damage (no crit, potency, or resistance)
	public static void damage(int damage, Creature victim, Color color, boolean force) {
		if ((victim.canHurt() || force) && victim.hurtCooldown != -1) {
			if (victim.trueDamageResistant && !force) {
				damage = 0;
			}
			victim.changeHealth((int)damage);
			if (victim.getHealthbar().getHealth() > 0) {
				victim.hurt(!force, Color.RED);
			} else {
				victim.setShouldDelete(true);
				Realms.getGame().getRoom().addEffects(Particle.create(Type.DEATH, victim.centerX(), victim.centerY(), 3, 3, 20, victim.getWidth(), false, true));
			}
			Realms.getGame().getRoom().addEffect(Particle.create(new Text(Integer.toString(damage), color, Format.GRADIENT), victim.centerX(), victim.centerY(), 5, 5, 40));
			victim.setColor(DamageOverTime.DAMAGE);
		}
	}
	
	public static void damage(int damage, Creature victim, boolean force) {
		damage(damage, victim, Color.RED, force);
	}
	
	public static void damageToKill(Creature victim) {
		damage(-victim.getHealthbar().getHealth(), victim, true);
	}
	
	public void kill() {
		//assess loot
		if (getType() != MovingObjects.KRAKEN_TENTACLE) {
			if (!noMiniHearts) {
				float min = .1f;
				float max = .3f;
				double count = min + (Realms.random() + getBadgeValue(Badge.Type.BLOODTHIRSTER))*(max - min);
				for (int i = 0; i < count*healthbar.getHealthMax(); i++) {
					new Loot(new Heart(Heart.Type.HEALTH_MINI, 1, ""), 1, 1, 1, false).drop(this, 0);;
				}
			}
			if (!noLoot) {
				for (Loot loot : loot) {
					loot.drop(this, lootBoost + (killer == null ? 0 : killer.getBadgeValue(Badge.Type.SCAVENGING)));
				}
			}
		}
		//kill message
		if (name != null) {
			Realms.getGame().getNotifs().add("> " + name + " was killed!", getType().color);
		}
		//write scroll
		if (killer == Realms.getGame().getPlayer()) {
			Scroll scroll = Realms.getGame().getPlayer().get(Scroll.class);
			if (scroll != null) {
				scroll.write(this);
			}
		}
		//clear sword animations
		for (int i = 0; i < Realms.getGame().getRoom().getEffects().size(); i++) {
			EffectAnimation effect = Realms.getGame().getRoom().getEffects().get(i);
			if (effect instanceof SwordAnimation && ((SwordAnimation)effect).getUser() == this) {
				Realms.getGame().getRoom().getEffects().remove(i);
				i--;
			}
		}
		//ai
		if (ai != null) {
			ai.kill();
		}
		if (divisibility != null) {
			divisibility.divide(this, Realms.getGame().getRoom());
		}
		//music
		if (music != null) {
			Audio.fade = -1;
			Audio.fadeDelayQueue = 60;
		}
		if (getType().deathSound() != null) {
			Audio.play(getType().deathSound(), .45f, Realms.vary(1f, 0.25f), centerX());
		}
		//other
		getType().kill(this);
		if (creatureType != null) {
			creatureType.kill(this);
		}
		Realms.getGame().getRoom().getBlocks().remove(nullZone);
		BloodAmulet ba = get(BloodAmulet.class);
		if (ba != null) {
			ba.heal();
		}
	}
	
	public double modifyDamage(CombatGear item, double damage) { 
		return damage;
	}
	
	public void dash(double x, double y) {
		addForce((int)x, (int)y);
		Audio.play(Sounds.FIRE, 1f, .5f, Realms.WIDTH/2);
	}
	
	public void addEssence(Essences essence, int potency) {
		//if has a shield, adds essence to shield, otherwise add shield and essence
		Shield shield = getShield();
		if (shield == null) {
			shield = new ShieldPassive("", new Statistics(StatType.RESISTANCE, 0, StatType.COUNT, 0), (Items)null, false);
			addItem(shield);
		}
		shield.addEssence(essence, potency);
	}
	
	public CombatGear weaponMelee() {
		for (Item item : items) {
			if (item instanceof CombatGear && ((CombatGear)item).isMelee()) {
				return (CombatGear)item;
			}
		}
		return null;
	}
	
	public CombatGear weaponRanged() {
		for (Item item : items) {
			if (item instanceof CombatGear && ((CombatGear)item).isRanged()) {
				return (CombatGear)item;
			}
		}
		return null;
	}
	
	public boolean preferMelee() {
		for (Item item : items) {
			if (item instanceof CombatGear) {
				if (((CombatGear)item).isMelee()) {
					return true;
				} else if (((CombatGear)item).isRanged()) {
					return false;
				}
			}
		}
		return false;
	}
	
	public Item findAmmo(Item item, boolean remove) {
		if (this instanceof Player) {
			return Realms.getGame().gui().inventory().findAmmo(item, remove);
		}
		return Game.search(items, item.getClass(), true);
	}
	
	public static Creature getCreature(int ID, Room room) {
		for (Creature creature : room.getCreatures()) {
			if (creature.ID == ID) {
				return creature;
			}
		}
		return null;
	}
	
	//gear
/*	@SuppressWarnings("unchecked")
	public <T extends Item> T findGear(Item item) {
		if (this instanceof Player) {
			return (T)Realms.getGame().gui().inventory().getGear(item);
		}
		return (T)Game.search(items, item, false);
	}*/
	
	@SuppressWarnings("unchecked")
	public <T extends Item> T findGear(Class<?> className) {
		if (this instanceof Player) {
			return (T)Realms.getGame().gui().inventory().getGear(className);
		}
		return (T)Game.search(items, className, false);
	}
	
	public ArrayList<Item> findAllGear(Item item) {
		if (this instanceof Player) {
			return Realms.getGame().gui().inventory().getAllGear(item);
		}
		return Game.search(items, item);
	}
	
	public Wearable findBadge(Badge.Type badge) {
		if (this instanceof Player) {
			return Realms.getGame().gui().inventory().getBadge(badge);
		}
		return Game.search(items, badge);
	}
	
	public double getBadgeValue(Badge.Type badge) {
		try {
			return findBadge(badge).getValue();
		} catch (NullPointerException e) { }
		return 0;
	}
	
	public ShieldPassive getShield() {
		return (ShieldPassive)findBadge(Badge.Type.RESISTANCE);
	}
	
	public double getResistance(Statistics statistics) {
		ShieldPassive item = getShield();
		if (item != null && (!item.getBackVulnerable() || !Realms.indexToDir(item.getOwner().direction).equals(statistics.getDir()))) {
			return item.stats().getStat(StatType.RESISTANCE);
		}
		return 0;
	}
	
	public double getActiveResistance(Statistics statistics) {
		ShieldActive item = get(ShieldActive.class);
		if (item != null && item.getActive() && (!item.getBackVulnerable() || !Realms.indexToDir(item.getOwner().direction).equals(statistics.getDir()))) {
			return item.stats().getStat(StatType.RESISTANCE);
		}
		return 0;
	}
	
	public Shields getShieldType() {
		try {
			return getShield().getShieldType();
		} catch (NullPointerException e) { }
		return null;
	}
	
	public double getCritResistance() {
		try {
			return getShield().stats().getStat(StatType.CRIT);
		} catch (NullPointerException e) { }
		return 0;
	}
	
	public double getFireResistance() {
		try {
			Attribute attribute = getShield().stats().getAttribute(Special.FIRE_RESISTANCE);
			if (attribute != null && Realms.random() < attribute.getValue()) {
				return attribute.getValue();
			}
		} catch (NullPointerException e) { }
		return 0;
	}
	
	public ArrayList<Essence> getEssences() {
		try {
			return getShield().getEssences();
		} catch (NullPointerException e) { }
		return null;
	}
	
	public SpikeHelmet getSpikeHelmet() {
		SpikeHelmet item = get(SpikeHelmet.class);
		if (item != null && item.canUse()) {
			return item;
		}
		return null;
	}
	
	public int getCloakDuration() {
		try {
			return get(Cloak.class).getDuration();
		} catch (NullPointerException e) { }
		return -1;
	}
	
	public double getReflectorChance(Projectile projectile) {
		Reflector item = get(Reflector.class);
		if (item != null && (!item.getBackVulnerable() || !Realms.indexToDir(item.getOwner().direction).equals(projectile.getDrop().stats().getDir()))) {
			return item.getChance();
		}
		return 0;
	}
	
	public boolean getBootTread() {
		try {
			return ((Boots)findBadge(Badge.Type.SPEED)).getTread();
		} catch (NullPointerException e) { }
		return false;
	}
	
	public boolean getBootIce() {
		try {
			return ((Boots)findBadge(Badge.Type.SPEED)).getIce();
		} catch (NullPointerException e) { }
		return false;
	}
	
	public int getTorchStrength() {
		try {
			return get(Torch.class).getStrength();
		} catch (NullPointerException e) { }
		return 0;
	}
	
	public Color getTorchColor() {
		try {
			return get(Torch.class).getColor();
		} catch (NullPointerException e) { }
		return Color.WHITE;
	}
	
	public double getProximitySuitReduction() {
		try {
			return get(ProximitySuit.class).getReduction();
		} catch (NullPointerException e) { }
		return 0;
	}
	
	public double getDiffuserChance() {
		try {
			return get(Diffuser.class).getChance();
		} catch (NullPointerException e) { }
		return 0;
	}
	
	public boolean getRustedGold(Item item) {
		RustedGold gold = get(RustedGold.class);
		return gold != null && new Entity(centerX(), centerY(), gold.getRadius(), gold.getRadius(), true).touching(item) && item instanceof CombatGear && ((CombatGear)item).getModded();
	}
	
	public <T extends Item> T get(Class<T> c) {
		return findGear(c);
	}
	
	public double getEnchantedFangChance() {
		try {
			return get(EnchantedFang.class).getChance();
		} catch (NullPointerException e) { }
		return 0;
	}
	
	/*public <T extends Item> T getClass(Class<?> clazz) {
		try {
//			Class<?> clazz = Class.forName(className);
//			System.out.println(clazz);
//			System.out.println(RustedGold.class);
			Constructor<?> ctor = clazz.getConstructor(String.class);
			@SuppressWarnings("unchecked")
			T object = (T)ctor.newInstance();
			return object;
		} catch (NoSuchMethodException e) { } catch (InvocationTargetException e) { } catch (IllegalAccessException e) { } catch (InstantiationException e) { }
		return null;
	}*/
	
	//specific
	public Creature setName(String name) {
		this.name = name;
		healthbar.setName(name);
		return this;
	}
	
	public Creature setNameIfNone(String name) {
		if (this.name == null) {
			setName(name);
		}
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public void addLoot(Loot loot) {
		this.loot.add(loot);
	}
	
	public ArrayList<Item> getItems() {
		return items;
	}
	
	public Creature addItem(Item item) {
		item.setOwner(this);
		items.add(item);
		return this;
	}
	
	public Intelligence getAI() {
		return ai;
	}
	
	public void setAI(Intelligence ai) {
		this.ai = ai;
		if (ai != null) {
			this.divisibility = ai.getDivisibility();
			ai.setUser(this);
		}
	}
	
	public Creature setDivisibility(Divisibility divisibility) {
		this.divisibility = divisibility;
		return this;
	}
	
	public Creatures getCreatureType() {
		return creatureType;
	}
	
	//movement
	public void freeze(float duration) {
		if (!shouldDelete()) {
			freezeDuration = duration;
			for (int i = 0; i < dots.size(); i++) {
				if (dots.get(i).getParticle() == Type.FIRE) {
					dots.remove(i);
					i--;
				}
			}
			Audio.play(Sounds.ICE, centerX());
		}
	}
	
	public float getFreeze() {
		return freezeDuration;
	}
	
	public void unFreeze() {
		if (freezeDuration != 0) {
			freezeDuration = 0;
			Audio.play(Sounds.ICE_BREAK, centerX());
			Realms.getGame().getRoom().addEffects(Particle.create(Particle.Type.ICE_BREAK, centerX(), centerY(), 2, 2, 10, 80));
		}
	}
	
	public double getSpeedBase() {
		return speedBase;
	}
	
	public void setSpeedBase(double speedBase) {
		this.speedBase = speedBase;
	}
	
	public void updateSpeed() {
		setSpeed(Realms.add(speedBase, getBadgeValue(Badge.Type.SPEED))*Realms.add(1, -speedReduction));
		speedReduction = 0;
	}
	
	public void changeSpeedBase(double amt) {
		speedBase += amt;
	}
	
	public double getSpeedReduction() {
		return speedReduction;
	}

	public void setSpeedReduction(double speedReduction) {
		this.speedReduction = speedReduction;
	}
	
	//damage
	public void changeHealth(int health) {
		if (health < 0) {
			healthbar.changeHealth(health*(Realms.random() < getEnchantedFangChance() ? -1 : 1), centerX(), centerY());
		} else {
			healthbar.changeHealth(health, centerX(), centerY());
		}
	}
	
	public HealthBar getHealthbar() {
		return healthbar;
	}
	
	public void setHealthbar(HealthBar healthbar) {
		this.healthbar = healthbar;
	}
	
	public ArrayList<DamageOverTime> getDOTs() {
		return dots;
	}

	public void addDOT(DamageOverTime dot) {
		dots.add(dot);
	}
	
	public void addEffect(Effect effect) {
		if (effect.getType().stackable) {
			for (Effect e : effects) {
				if (e.getType() == effect.getType()) {
					e.extend(effect.getDuration());
					return;
				}
			}
		}
		effects.add(effect);
	}

	public ArrayList<Effect> getEffects() {
		return effects;
	}
	
	public void clearEffects() {
		for (Effect effect : effects) {
			effect.end();
		}
	}
	
	public void setLootBoost(double lootBoost) {
		this.lootBoost = lootBoost;
	}
	
	public ArrayList<Loot> getLoot() {
		return loot;
	}
	
	public void setKiller(Creature killer) {
		this.killer = killer;
	}
	
	public void hurt(boolean resetCooldown, Color color) {
		if (resetCooldown) {
			hurtCooldown = HURT_COOLDOWN;
		}
		hurtCooldownColor = HURT_COOLDOWN;
		if (this instanceof Player) {
			((Player)this).getAnim().worsen(color);
		}
	}
	
	public boolean canHurt() {
		return hurtCooldown == 0;
	}
	
	public int getHurtCooldown() {
		return hurtCooldown;
	}
	
	public void resetHurtCooldown() {
		hurtCooldown = HURT_COOLDOWN;
	}
	
	public void refreshHurtCooldown() {
		hurtCooldown = 0;
	}
	
	public void setImmune() {
		hurtCooldown = -1;
	}
	
	//drawing
	public void setDirection(int index) {
		if (this instanceof Player) {
			direction = index;
			((Player)this).changeDirection(index == 0 ? Dirs.DOWN : index == 1 ? Dirs.UP : index == 2 ? Dirs.LEFT : Dirs.RIGHT);
		} else {
			setSprite(getType().textures[textureIndex][direction = index]);
		}
	}
	
	public int getDirection() {
		return direction;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public void setColor2(Color color2) {
		this.color2 = color2;
	}
	
	public void setA(float a) {
		this.a = a;
	}
	
	public float totalA() {
		return a*(hidden || !cloaked ? 1f : .25f);
	}
	
	public boolean getRotates() {
		return rotates;
	}
	
	public void setRotates(boolean rotates) {
		this.rotates = rotates;
	}
	
	//effects & options
	public void setCloaked(boolean cloaked) {
		this.cloaked = cloaked;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public boolean invisibleToAI() {
		return cloaked || hidden;
	}
	
	public void setBlind(boolean blind) {
		this.blind = blind;
	}
	
	public boolean getBlind() {
		return blind;
	}
	
	public boolean getClairvoyant() {
		return clairvoyant;
	}
	
	public void setClairvoyant(boolean clairvoyant) {
		this.clairvoyant = clairvoyant;
	}
	
	public boolean getDyslexic() {
		return dyslexic;
	}
	
	public void setDyslexic(boolean dyslexic) {
		this.dyslexic = dyslexic;
	}
	
	public boolean getDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public void setDisablePickup(boolean disablePickup) {
		this.disablePickup = disablePickup;
	}
	
	public ModBlock getNullZone() {
		return nullZone;
	}
	
	public void setNullZone(int radiusA, int radiusB, int nullZoneDuration, Room room, Texture texture) {
		nullZone = new ModBlock(centerX(), centerY(), radiusA, radiusB, true, Modification.NON_PROJECTILES_ONLY, texture, 0);
		nullZone.setSolid(false);
		nullZone.setOwner(this);
		room.addBlock(nullZone);
		this.nullZoneDuration = nullZoneDuration;
	}
	
	public void setNullZone(int radius, int nullZoneDuration, Room room) {
		setNullZone(radius, radius, nullZoneDuration, room, ScreenEffects.NULLZONE.texture);
	}
	
	public void setTrueDamageResistant(boolean trueDamageResistant) {
		this.trueDamageResistant = trueDamageResistant;
	}
	
	public boolean getTrueDamageResistant() {
		return trueDamageResistant;
	}
	
	public boolean getExplosionResistant() {
		return explosionResistant;
	}
	
	public void setExplosionResistant(boolean explosionResistant) {
		this.explosionResistant = explosionResistant;
	}
	
	public void setNoMiniHearts(boolean noMiniHearts) {
		this.noMiniHearts = noMiniHearts;
	}
	
	public void setNoLoot(boolean noLoot) {
		this.noLoot = noLoot;
	}
	
	public boolean getNoMiniHearts() {
		return noMiniHearts;
	}
	
	public boolean getNoLoot() {
		return noLoot;
	}
	
	public void setCanGoOffscreen(boolean canGoOffscreen) {
		this.canGoOffscreen = canGoOffscreen;
	}
	
	//other
	public Musics getMusic() {
		return music;
	}
	
	public void setMusic(Musics music) {
		this.music = music;
	}
	
//	public void setSprite(int index) {
//		super.setSprite(getType().textures[textureIndex][index]);
//	}

}
