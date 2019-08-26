package com.mygdx.game.entity;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Audio;
import com.mygdx.game.Audio.Sounds;
import com.mygdx.game.Statistics.StatType;
import com.mygdx.game.Essence;
import com.mygdx.game.Graphics;
import com.mygdx.game.Loot;
import com.mygdx.game.Realms;
import com.mygdx.game.gui.HealthBar.Display;
import com.mygdx.game.item.Item;
import com.mygdx.game.item.gear.CombatGear;
import com.mygdx.game.item.gear.Consumable;
import com.mygdx.game.item.weapon.Bomb;
import com.mygdx.game.item.weapon.Bomb.Ability;
import com.mygdx.game.item.weapon.Crossbow;
import com.mygdx.game.item.weapon.Dagger;
import com.mygdx.game.item.weapon.Hammer;

public abstract class MovingObject extends Entity {
	
	private MovingObjects type;
	private Group group;
	private double speed;
	private Vector2 forceMain;
	private Vector2 force;
	private int rotation;
	private boolean immovable;
	private Vector2 lastPosition;
	private boolean permeable;
	private float gameSpeed;
	private static final Color timeChange = new Color(0f, 0f, 1f, 0f);
	
	public MovingObject(Group group, int x, int y, int width, int height, double velX, double velY, MovingObjects type, TextureRegion sprite) {
		super(x, y, width, height, false, sprite);
		this.group = group;
		this.type = type;
		forceMain = new Vector2((float)velX, (float)velY);
//		forceMain = new Vector2((float)((int)(velX + .5)), (float)((int)(velY + .5)));
		force = new Vector2();
		lastPosition = new Vector2();
		speed = velX;
		gameSpeed = 1;
	}
	
	public MovingObject(Group group, int x, int y, int width, int height, double velX, double velY, MovingObjects type, Texture texture) {
		this(group, x, y, width, height, velX, velY, type, Realms.toRegion(texture));
	}
	
	public MovingObject(Group group, int x, int y, int width, int height, MovingObjects type) {
		this(group, x, y, width, height, 0, 0, type, type.textures[(int)(Math.random()*type.texture.length)][0]);
	}
	
	
	public enum MovingObjects {
		FACE(Color.WHITE, "Face", "creature/enemy.png"),
		SPIDER(Color.WHITE, "Spider", "creature/enemy2.png"),
		SPIKEBALL(Color.ROYAL, "Spikey", "creature/red.png"),
		GREEN(Color.GREEN, "Greeny", "creature/green.png"),
		BLOOD_MONSTER(Color.RED, "Blood Monster", "creature/blood_monster.png"),
		KNIGHT(Color.BLUE, "Knights", "creature/knight.png", "creature/knight2.png", "creature/knight3.png"),
		KNIGHT_LITTLE(Color.GREEN, "Lil' Knight", "creature/knight_little.png"),
		BRUISER(Color.ORANGE, "Bruiser", "creature/bruiser.png"),
		SORCERER(Color.PURPLE, "Sorcerer", "creature/sorcerer.png", "creature/sorcerer2.png"),
		RHINEEOSE(Color.FIREBRICK, "Dweller", "creature/rhineeose.png"),
		SCAVENGER(Color.BROWN, "Scavenger", "creature/bokoblin2.png"),
		FLY(Color.ROYAL, "Gnat", "creature/fly1.png", "creature/fly2.png", "creature/fly3.png"),
		VAMP(Color.RED, "Vamp", "creature/blood_monster.png"),
		CREEP(Color.WHITE, "Haunt", "creature/creep.png"),
		ZOMBIE(Color.TEAL, "Zombie", "creature/zombie1.png", "creature/zombie2.png", "creature/zombie3.png", "creature/zombie4.png"),
		ZOMBIE_FIRE(Color.RED, "Fiery Zombie", "creature/zombie_fire.png"),
		IGNITO(Color.ORANGE, "Ignito", "creature/knight_little.png"),
		MOSSMAN(Color.GREEN, "Mossman", "creature/green.png"),
		SAND_GUARD(Color.TAN, "Sand Guard", "creature/sand_guard.png"),
		MANTRIS(Color.ROYAL, "Mantris", "creature/red.png"),
		ALFOID(Color.RED, "Alfoid", "creature/alfoid.png"),
		HAMMERHEAD(Color.GREEN, "Hammerhead", "creature/knight_little.png"),
		SEASPIKE(Color.NAVY, "Seaspike", "creature/seaspike.png", "creature/seaspike2.png"),
		NECROMANSKULL(Color.PURPLE, "Necromanskull", "creature/necromanskull.png", "creature/necromanskull2.png"),
		SCORPIO(Color.BLUE, "Scorpio", "creature/scorpio.png"),
		NASTY(Color.OLIVE, "Nasty", "creature/nastytacks.png"),
		COSMICER(Color.ROYAL, "Cosmicer", "creature/cosmicer.png"),
		MIMIC(Color.TAN, "Mimic", "creature/mimic.png"),
		DARKFLY(Color.GRAY, "Dark Fly", "creature/darkfly.png"),
		FLYMOUTH(Color.GRAY, "Darkfly Spawner", "creature/flymouth.png"),
		WALL_SPITTER(Color.FIREBRICK, "Wall Spitter", "creature/wall_spitter.png"),

		AXE_HOSH(Color.RED, "Axe Hosh", "creature/skel/axe_hosh.png"),
		BELLATRIX(Color.MAGENTA, "Bellatrix", "creature/skel/bellatrix.png"),
		CLASPER(Color.SCARLET, "Clasper", "creature/skel/clasper.png"),
		DANGER(Color.RED, "Danger", "creature/skel/danger.png"),
		DEVOR(Color.RED, "Devor", "creature/skel/devor.png"),
		DHARRONN(Color.PURPLE, "Dharronn", "creature/skel/dharronn.png"),
		DROTAK(Color.CHARTREUSE, "Drotak", "creature/skel/drotak.png"),
		ERNIE(Color.ROYAL, "Ernie", "creature/skel/ernie.png"),
		EVORON(Color.GOLD, "Evoron", "creature/skel/evoron.png"),
		F2_Y2(Color.OLIVE, "F2 Y2", "creature/skel/f2-y2.png"),
		GARMARADON(Color.FIREBRICK, "Garmardon", "creature/skel/garmardon.png"),
		GEARZHOCK(Color.LIGHT_GRAY, "Gearzhock", "creature/skel/gearzhock.png"),
		HACKET(Color.RED, "Hacket", "creature/skel/hacket.png"),
		HATCHET(Color.SKY, "Hatchet", "creature/skel/hatchet.png"),
		HEXTOR(Color.FIREBRICK, "Hextor", "creature/skel/hextor.png"),
		IAVON(Color.LIME, "Iavon", "creature/skel/iavon.png"),
		LEO(Color.ORANGE, "Leo", "creature/skel/leo.png"),
		PIRANHA(Color.ROYAL, "Piranha", "creature/skel/piranha.png"),
		ROBIO(Color.GREEN, "Wisp", "creature/skel/robio.png"),
		ROCKHEAD(Color.BLUE, "Dweller", "creature/skel/rockhead.png"),
		RUPT_CANO(Color.FIREBRICK, "Rupt-Cano", "creature/skel/rupt-cano.png"),
		SABOR(Color.GRAY, "Sabor", "creature/skel/sabor.png"),
		SCORDRAL(Color.CYAN, "Scordral", "creature/skel/scordral.png"),
		SHARK_SABRE(Color.ORANGE, "Pirate", "creature/skel/shark_sabre.png"),
		SISORN(Color.RED, "Sisorn", "creature/skel/sisorn.png"),
		SKYTRIXX(Color.LIME, "Skytrixx", "creature/skel/skytrixx.png"),
		TANKHEAD(Color.FOREST, "Tankhead", "creature/skel/tankhead.png"),
		TRIKE(Color.DARK_GRAY, "Trike", "creature/skel/trike.png"),
		ZIGHTOR(Color.GOLD, "Zightor", "creature/skel/zightor.png"),
		ZIP(Color.GOLD, "Zip", "creature/skel/zip.png"),
		STONE_BONES(Color.GRAY, "Stone Bones", "creature/skel/stone_bones.png"),
		LIGHTNING_HOSH(Color.YELLOW, "Lightning Hosh", "creature/skel/lightning_hosh.png"),
		
		KRAKEN(Color.ROYAL, null, Display.BOSS, "creature/kraken.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		KRAKEN_TENTACLE(Color.ROYAL, null, Display.NONE, "creature/red.png") {
			public void kill(Creature creature) { killBoss(creature); }
			
			public Sounds deathSound() {
				return null;
			}
		},
		GOLEM(Color.GRAY, null, Display.BOSS, "creature/mightron.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		ROGUE(Color.GRAY, null, Display.BOSS, "creature/necromanskull.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		DWELLER_BOSS(Color.FIREBRICK, null, Display.BOSS, "creature/rhineeose.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		MINOTAUR(Color.DARK_GRAY, null, Display.BOSS, "creature/bruiser.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		FIRE_BOSS(Color.RED, null, Display.BOSS, "creature/knight_little.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		FLYNEST(Color.WHITE, null, Display.BOSS, "creature/fly1.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		ICEBOSS(Color.WHITE, null, Display.BOSS, "creature/iceboss.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		ICEBOSS_NOEYES(Color.WHITE, null, Display.BOSS, "creature/iceboss_noeyes.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		ARCHER_BOSS(Color.OLIVE, null, Display.BOSS, "creature/nastytacks.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		SHADOW_BOSS(Color.WHITE, null, Display.BOSS, "creature/creep.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		NINJA_BOSS(Color.WHITE, null, Display.BOSS, "creature/creep.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		IDOL_BOSS(Color.RED, null, Display.BOSS, "creature/alfoid.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		TRASH_BOSS(Color.GRAY, null, Display.BOSS, "block/material/metal.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		ZOMBIE_BOSS(Color.TEAL, null, Display.BOSS, "creature/zombie1.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		BASILISK(Color.OLIVE, null, Display.BOSS, "creature/box.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		SAND_BOSS(Color.GOLD, null, Display.BOSS, "creature/sand_boss.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		MIRROR_BOSS(Color.DARK_GRAY, null, Display.BOSS, "creature/mirror.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		ROCK_BOSS(Color.GRAY, null, Display.BOSS, "creature/mightron.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		ELETRIC_BOSS(Color.GRAY, null, Display.BOSS, "creature/lutron.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		ZOMBIE_FIRE_BOSS(Color.GRAY, null, Display.BOSS, "creature/zombie_fire.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		SPECTRESS_BOSS(Color.GOLD, null, Display.BOSS, "creature/skel/evoron.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		PURPLE_POISON_BOSS(Color.PURPLE, null, Display.BOSS, "creature/nastytacks.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		EYE_LASER_BOSS(Color.RED, null, Display.BOSS, "creature/blank.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		LICH_BOSS(Color.RED, null, Display.BOSS, "creature/skel/evoron.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		SLOTH(Color.SLATE, null, Display.BOSS, "creature/sloth.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		LUST(Color.RED, null, Display.BOSS, "creature/lust.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		GLUTTONY(Color.VIOLET, null, Display.BOSS, "creature/gluttony.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		WRATH(Color.DARK_GRAY, null, Display.BOSS, "creature/wrath.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		GREED(Color.GOLD, null, Display.BOSS, "creature/greed.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},
		ENVY(Color.GREEN, null, Display.BOSS, "creature/envy.png") {
			public void kill(Creature creature) { killBoss(creature); }
		},


		BOLT {
			public void collideBlock(Projectile projectile, Block block) {
				if (block instanceof ModBlock) {
					((ModBlock)block).effect(projectile);
				} else if (block.getSolid()) {
					drop(projectile, block);
				}
				block.getType().effect(block, projectile.getDrop());
			}
			
			public void drop(Projectile projectile, Block block) {
				Audio.play(Sounds.CROSSBOW_HIT, 0.5f, 1.5f, projectile.centerX());
				projectile.setShouldDelete(true);
			}
			
			public void collideCreature(Projectile projectile, Creature creature) {
				if (creature.getReflectorChance(projectile) > Math.random() && !projectile.getPermeable()) {
					projectile.reverse(creature);
				} else {
					if (((Crossbow)projectile.getDrop()).getPassThrough()) {
						if (creature.getGroup() != projectile.getUser().getGroup()) {
							Audio.play(Sounds.CROSSBOW_HIT, 1f, Realms.vary(1.5f, 0.1f), projectile.centerX());
						}
					} else {
						Audio.play(Sounds.CROSSBOW_HIT, 1f, Realms.vary(1f, 0.1f), projectile.centerX());
					}	
					hit(projectile, creature);
				}
			}
			
			public Texture getTexture(CombatGear item) {
				return ((Crossbow)item).getBolt();
			}
		},
		BOMB {
			public void collideBlock(Projectile projectile, Block block) { 
				if (block instanceof ModBlock) {
					((ModBlock)block).effect(projectile);
				} else if (block.getSolid()) {
					drop(projectile, block);
				}
			}
			
			public void drop(Projectile projectile, Block block) {
				if (((Bomb)projectile.getDrop()).getAbility() == Ability.IMPACT) {
					projectile.explode();
				} else {
					projectile.unCollideAll();
					projectile.resetAllPhysics();
				}
			}
			
			public void collideCreature(Projectile projectile, Creature creature) { 
				if (((Bomb)projectile.getDrop()).getAbility() == Ability.IMPACT) {
					projectile.explode();
				} else {
					projectile.unCollide(creature);
					projectile.resetAllPhysics();
				}
			}
			
			public Texture getTexture(CombatGear item) {
				return ((Bomb)item).getBomb().texture;
			}
		},
		DAGGER {
			public void collideBlock(Projectile projectile, Block block) { 
				if (block instanceof ModBlock) {
					((ModBlock)block).effect(projectile);
				} else if (block.getSolid()) {
					drop(projectile, block);
				}
				block.getType().effect(block, projectile.getDrop());
			}
			
			public void drop(Projectile projectile, Block block) {
				Audio.play(Sounds.SPEAR_HIT, 0.5f, 1f, projectile.centerX());
				projectile.setShouldDelete(true);
				if (block != null && !block.getCircular() && !(block instanceof Polygon)) {
					projectile.drop(projectile.getDrop().clone(), block);
				} else {
					if (!(block instanceof Polygon)) {
						projectile.unCollide(block);
					}
					projectile.drop(projectile.getDrop().clone());
				}
			}
			
			public void collideCreature(Projectile projectile, Creature creature) { 
				if (creature.getReflectorChance(projectile) > Math.random()) {
					projectile.reverse(creature);
				} else {
					if (projectile.getDrop() instanceof Dagger && ((Dagger)projectile.getDrop()).getPassThrough()) {
						if (creature.getGroup() != projectile.getUser().getGroup()) {
							Audio.play(Sounds.SPEAR_HIT, 1f, 0.75f, projectile.centerX());
						}
					} else {
						Audio.play(Sounds.BREAK, 1f, Realms.vary(0.75f, 0.1f), projectile.centerX());
					}	
					hit(projectile, creature);
				}
			}
		},
		MACE {
			public void collideBlock(Projectile projectile, Block block) { 
				if (block instanceof ModBlock) {
					((ModBlock)block).effect(projectile);
				} else if (block.getSolid()) {
					drop(projectile, block);
				}
			}
			
			public void drop(Projectile projectile, Block block) {
				projectile.unCollideAll();
				projectile.resetForce();
			}
			
			public void collideCreature(Projectile projectile, Creature creature) { 
				projectile.getUser().damage(projectile.getDrop(), creature, false, projectile.getDamageBoost() + projectile.getDrop().stats().getEmpowerDamage());
				projectile.setPullback(true);
				Audio.play(Sounds.SWORD_HIT, 1f, Realms.vary(0.5f, 0.1f), projectile.centerX());
			}
			
		},
		BOOMERANG {
			public void collideBlock(Projectile projectile, Block block) { 
				if (block.getSolid()) {
					projectile.setPullback(true);
//					projectile.unCollide(block);
					Audio.play(Sounds.CROSSBOW_HIT, 0.5f, 1.5f, projectile.centerX());
//					projectile.setShouldDelete(true);
//					Item.drop(projectile.getDrop().clone(), projectile.getX(), projectile.getY(), 0, 0, 0, 60);
				}
			}
			
			public void collideCreature(Projectile projectile, Creature creature) { 
				Audio.play(Sounds.BREAK, 1f, 1.5f, projectile.centerX());
				hit(projectile, creature);
			}
			
			public Texture getTexture(CombatGear item) {
				return item.getItem().texture;
			}
		},
		POTION {
			public void collideBlock(Projectile projectile, Block block) { 
				if (block instanceof ModBlock) {
					((ModBlock)block).effect(projectile);
				} else if (block.getSolid()) {
					drop(projectile, block);
				}
			}
			
			public void drop(Projectile projectile, Block block) {
				projectile.splash();
			}
			
			public void collideCreature(Projectile projectile, Creature creature) { 
				projectile.splash();
			}
		
			public Texture getTexture(CombatGear item) {
				return ((Consumable)item).getColor().texture;
			}
			//type == MovingObjects.POTION ? ((Consumable)drop).getColor().texture : type == MovingObjects.BOMB ? ((Bomb)drop).getBomb().texture : type == MovingObjects.BOLT ? ((Crossbow)drop).getBolt() : type.texture[0]
		},
		
		BOX("creature/box.png"),
		BOX2("creature/box2.png"),
		BLANK(null, null, Display.NONE, "creature/blank.png") {
			public Sounds deathSound() {
				return null;
			}
		},
		PLAYER(null, null, Display.PLAYER, "creature/player.png"),
		PLAYERWHITE("creature/player_white.png");
		
		public final Texture[] texture;
		public final TextureRegion[][] textures;
		public final Color color;
		public final String name;
		public final Display display;
		
		private MovingObjects(Color color, String name, Display display, String... texture) {
			this.texture = new Texture[texture.length];
			for (int i = 0; i < this.texture.length; i++) {
				this.texture[i] = new Texture("entity/" + texture[i]);
			}
			textures = new TextureRegion[texture.length][];
			for (int i = 0 ; i < this.textures.length; i++) {
				this.textures[i] = Realms.divideIntoFour(this.texture[i]);
			}
//			this.textures = Realms.divideIntoFour(texture);
			this.color = color;
			this.name = name;
			this.display = display;
		}
		
		private MovingObjects(Color color, String name, String... texture) {
			this(color, name, Display.DEFAULT, texture);
		}
		
		private MovingObjects(String... texture) {
			this(null, null, (Display)null, texture);
		}
		
		public void collideBlock(Projectile projectile, Block block) { }
		
		public void collideCreature(Projectile projectile, Creature creature) { }
		
		public void drop(Projectile projectile, Block block) { }
		
		public void hit(Projectile projectile, Creature creature) {
			if (projectile.getUser() == null) {
				Creature.damage(-projectile.getDrop().stats().getDamage(), creature, false);
			} else {
				projectile.getUser().damage(projectile.getDrop(), projectile.getGroup(), creature, false, (int)Realms.distance(projectile.getStartX(), projectile.getStartY(), projectile.centerX(), projectile.centerY()), Realms.arctan(creature.centerX() - projectile.centerX(), creature.centerY() - projectile.centerY()), false, projectile.getDrop().stats().getEmpowerDamage() + projectile.getDamageBoost());
			}
			if ((projectile.getType() == MovingObjects.BOLT && !((Crossbow)projectile.getDrop()).getPassThrough()) || (projectile.getDrop() instanceof Dagger && !((Dagger)projectile.getDrop()).getPassThrough()) || projectile.getDrop() instanceof Hammer) {
				projectile.setShouldDelete(true);
				if (projectile.getState() == 1 && projectile.getType() == MovingObjects.DAGGER && Math.random() < projectile.getDrop().stats().getStat(StatType.RESISTANCE)) {
					Item drop = projectile.getDrop().clone();
					drop.setSize(1);
					creature.addLoot(new Loot(drop, 1, 1, 1, false));
					projectile.setState(0);
				}
			}
		}
	
		public Texture getTexture(CombatGear item) { return null; }
		
		public void kill(Creature creature) { }
		
		public void killBoss(Creature creature) {
			if (creature.getHealthbar().getHealth() <= 0) {
				Realms.getGame().getRoom().getBoss().end();
			}
		}
		
		public Sounds deathSound() {
			return Sounds.DEATH;
		}
		
		public enum Lists {
			CREATURES(new ArrayList<MovingObjects>(Arrays.asList(MovingObjects.SPIKEBALL, MovingObjects.GREEN, MovingObjects.BLOOD_MONSTER, MovingObjects.KNIGHT, MovingObjects.BRUISER, MovingObjects.SORCERER, MovingObjects.KNIGHT_LITTLE))),
			;
			public final ArrayList<MovingObjects> list;
			
			private Lists(ArrayList<MovingObjects> list) {
				this.list = list;
			}
			
			public MovingObjects random() {
				MovingObjects mo;
				do {
					mo = Realms.random(MovingObjects.values());
				} while (mo.color == null || mo.name == null);
				return mo;
//				return list.get((int)(Math.random()*list.size()));
			}
		}
		
	}
	
	public enum Group {
		FRIEND,
		FOE,
		NEUTRAL;
		
		public Group opposite() {
			return this == Group.FRIEND ? Group.FOE : this == Group.FOE ? Group.FRIEND : Group.NEUTRAL;
		}
	}
	
	
	public void move(float freeze, float friction) {
		lastPosition.set(getX(), getY());
		if (forceMain != null && freeze <= 0 && !getImmovable()) {
			changeX(forceMain.x * gameSpeed);
			changeY(forceMain.y * gameSpeed);
			updateForce(forceMain, friction, gameSpeed);
		}
		if (force != null) {
			changeX(force.x * gameSpeed);
			changeY(force.y * gameSpeed);
			updateForce(force, friction, gameSpeed);
		}
	}
	
	public static void updateForce(Vector2 force, float friction, float gameSpeed) {
		force.x -= friction*Math.abs(Math.cos(Math.toRadians(force.angle())))*Math.signum(force.x) * gameSpeed;
		force.y -= friction*Math.abs(Math.sin(Math.toRadians(force.angle())))*Math.signum(force.y) * gameSpeed;
		if (nearZero(force.x, friction)) {
			force.x = 0;
		}
		if (nearZero(force.y, friction)) {
			force.y = 0;
		}
	}
	
	public static boolean nearZero(float value, float friction) {
		return Math.abs(value) < friction;
//		return (0 < value && 0 > value - friction) || (0 > value && 0 < value + friction);
	}
	
	public void unCollideAll() {
		if (touchingAnySolidBlocks()) {
			Vector2 pos = new Vector2(getX(), getY());
			boolean moveX, moveY;
			//if move to last x, then won't be colliding
			setX((int)lastPosition.x);
			moveX = !touchingAnySolidBlocks();
			setX((int)pos.x);
			//if move to last y, then won't be colliding
			setY((int)lastPosition.y);
			moveY = !touchingAnySolidBlocks();
			setY((int)pos.y);
			
			if (!moveX && !moveY) {
				moveX = moveY = true;
			}
//			if (!moveX && !moveY) {
//				//if move to last x and y, then won't be colliding
//				setX((int)lastPosition.x);
//				setY((int)lastPosition.y);
//				moveX = moveY = !touchingAnySolidBlocks();
//				setX((int)pos.x);
//				setY((int)pos.y);
//			}
			boolean polygon = false;
			for (Block block : Realms.getGame().getRoom().solidBlocks()) {
				if (block.touching(this) && block instanceof Polygon) {
					polygon = true;
				}
			}
			if (!polygon) {
				if (moveX) {
					resetVelX();
				}
				if (moveY) {
					resetVelY();
				}
			}
			int attempts = 0;
			while (touchingAnySolidBlocks() && attempts < 500) {
				//if moved + to get into block, then move -
				//if moved - to get into block, then move +
				if (moveX) {
					changeX(-(int)Math.signum(getX() - (int)lastPosition.x));
				}
				if (moveY) {
					changeY(-(int)Math.signum(getY() - (int)lastPosition.y));
				}
				attempts++; 
				
			}
		}
	}
	
	public boolean touchingAnySolidBlocks() {
		return entering(Realms.getGame().getRoom().solidBlocks());
	}
	
	public void unCollide(Entity entity) {
		if (entity != null) {
			Rectangle2D collision = toRect().createIntersection(entity.toRect());
			if (collision.getHeight() < collision.getWidth() && getY2() < entity.getY2()) {
				setY(entity.getY() - getHeight());
	        }
	        if (collision.getHeight() < collision.getWidth() && getY() > entity.getY()) {
	        	setY(entity.getY2());
	        }  
			if (collision.getHeight() > collision.getWidth() && getX() > entity.getX()) {
				setX(entity.getX2());
			}
	        if (collision.getHeight() > collision.getWidth() &&getX2() < entity.getX2()) {
	        	setX(entity.getX() - getWidth());
	        }
		}
	}
	
	public static double compareEssence(ArrayList<Essence> attack, ArrayList<Essence> resist) {
		if (resist != null) {
			int totalResistance = 0;
			for (Essence essenceR : resist) {
				int potency = essenceR.getPotency();
				if (attack != null) {
					for (Essence essenceA : attack) {
						if (essenceA.getEssence() == essenceR.getEssence()) {
							potency = Math.max(potency - essenceA.getPotency(), 0);
						}
					}
				}
				totalResistance += potency;
			}
			return Math.min(totalResistance * .1, 1);
		}
		return 0;
	}
	
	public boolean offscreenCompletely() {
		return (getX2() < 0 || getX() > Realms.getGame().getRoom().getXMax() || getY2() < 0 || getY() > Realms.getGame().getRoom().getYMax()) && getX() != -1337;
	}
	
	public void draw() {
		Graphics.begin();
		if (this instanceof Player) {
			((Player)this).draw2();
		} else if (this instanceof Creature && ((Creature)this).getRotates()) {
			Graphics.draw(getSprite(), getX(), getY(), getWidth()/2, getHeight()/2, getWidth(), getHeight(), 1, 1, rotation + 90);
		} else if (this instanceof ProjectileAdvanced) {
			Graphics.draw(getSprite(), centerX() - ((Crossbow)((Projectile)this).getDrop()).getLarge()/2, centerY() - ((Crossbow)((Projectile)this).getDrop()).getSmall()/2, ((Crossbow)((Projectile)this).getDrop()).getLarge()/2, ((Crossbow)((Projectile)this).getDrop()).getSmall()/2, ((Crossbow)((Projectile)this).getDrop()).getLarge(), ((Crossbow)((Projectile)this).getDrop()).getSmall(), 1, 1, ((ProjectileAdvanced)this).getAngle() + ((Projectile)this).getRotation2());
		} else {
			if (rotation % 180 == 0) {
				Graphics.draw(getSprite(), getX(), getY(), getWidth()/2, getHeight()/2, getWidth(), getHeight(), 1, 1, rotation);
			} else if (rotation == 90) { 
				Graphics.draw(getSprite(), getX(), getY(), getWidth()/2, getWidth()/2, getHeight(), getWidth(), 1, 1, rotation);
			} else {
				Graphics.draw(getSprite(), getX(), getY(), getHeight()/2, getHeight()/2, getHeight(), getWidth(), 1, 1, rotation);
			}
		}
		Graphics.end();
		drawBounds();
	}
	
	public void drawBounds() {
		if (Realms.getGame().getBounds()) {
			Graphics.shapeRenderer.begin(ShapeType.Line);
			Graphics.rect(getX(), getY(), getWidth(), getHeight());
			Graphics.shapeRenderer.rectLine(centerX(), centerY(), centerX() + (int)(sumVelX()*10), centerY() + (int)(sumVelY()*10), 3);
			Graphics.shapeRenderer.end();
		}
	}
	
	public float getGameSpeed() {
		return gameSpeed;
	}
	
	public void scaleGameSpeed(float scalar) {
		setGameSpeed(gameSpeed * scalar);
	}
	
	public void setGameSpeed(float gameSpeed) {
		if (this instanceof Player && this.gameSpeed != gameSpeed) {
			Realms.getGame().getPlayer().getAnim().worsenSpecial(timeChange.cpy());
		}
		this.gameSpeed = gameSpeed;
	}
	
	
	public Group getGroup() {
		return group;
	}
	
	public void setGroup(Group group) {
		this.group = group;
	}
	
	public MovingObjects getType() {
		return type;
	}
	
	public void setType(MovingObjects type) {
		this.type = type;
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public Vector2 getForceMain() {
		return forceMain;
	}
	
	public void resetForce() {
		forceMain.set(0, 0);
	}
	
	public void setForce(Vector2 forceMain) {
		this.forceMain = forceMain;
	}
	
	public void setForce(float x, float y) {
		forceMain.set(x, y);
	}

	public void resetVelX() {
		forceMain.x = 0;
		force.x = 0;
	}
	
	public void resetVelY() {
		forceMain.y = 0;
		force.y = 0;
	}
	
	public float sumVelX() {
		return forceMain.x + force.x;
	}
	
	public float sumVelY() {
		return forceMain.y + force.y;
	}
	
	public void addForce(Vector2 force) {
		this.force.add(force);
	}
	
	public void addForce(double x, double y) {
		force.add((float)x, (float)y);
	}
	
	public void addForce(float x, float y) {
		force.add(x, y);
	}
	
	public Vector2 getForce() {
		return force;
	}
	
	public void resetPhysics() {
		force.set(0, 0);
	}
	
	public void resetAllPhysics() {
		resetForce();
		resetPhysics();
	}
	
	public boolean idle() {
		return sumVelX() == 0 && sumVelY() == 0;
	}
	
	public int getRotation() {
		return rotation;
	}
	
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}
	
	public boolean getImmovable() {
		return immovable;
	}
	
	public void setImmovable(boolean immovable) {
		if (immovable) {
			resetPhysics();
		}
		this.immovable = immovable;
	}
	
	public Vector2 getLastPosition() {
		return lastPosition;
	}
	
	public boolean getPermeable() {
		return permeable;
	}
	
	public void setPermeable(boolean permeable) {
		this.permeable = permeable;
	}

}
