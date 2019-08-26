package com.mygdx.game.item;

import java.util.ArrayList;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.mygdx.game.Audio;
import com.mygdx.game.Audio.Sounds;
import com.mygdx.game.Chestable;
import com.mygdx.game.Essence;
import com.mygdx.game.Essence.Essences;
import com.mygdx.game.Game.Window;
import com.mygdx.game.Graphics;
import com.mygdx.game.Loot;
import com.mygdx.game.Realms;
import com.mygdx.game.Room;
import com.mygdx.game.Text;
import com.mygdx.game.effects.Light;
import com.mygdx.game.effects.SwordAnimation.Direction;
import com.mygdx.game.entity.Creature;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.entity.Player;
import com.mygdx.game.gui.Slot;
import com.mygdx.game.gui.PauseMenuButton.Option;
import com.mygdx.game.item.gear.Activatable;
import com.mygdx.game.item.gear.Badge;
import com.mygdx.game.item.gear.CombatGear;
import com.mygdx.game.item.gear.Consumable;
import com.mygdx.game.item.gear.Link;
import com.mygdx.game.item.gear.Material;
import com.mygdx.game.item.gear.Scroll;
import com.mygdx.game.item.gear.ShieldActive;
import com.mygdx.game.item.weapon.Bomb;
import com.mygdx.game.item.weapon.Weaponizable;
import com.mygdx.game.item.weapon.WindupWeapon;

public class Item extends Entity implements Chestable, Cloneable {
	
	private Type type;
	private Items item;
	private int size;
	private int sizeMax;
	private String name;
	private String id;
	private int pickupDelay;
	private int scriptIndex;
	private Creature owner;
	private ArrayList<Essence> essences;
	private boolean old;
	private boolean starter;
	private static int life;
	private int velocity;
	private boolean permeable;
	private ArrayList<FileHandle> scripts;
	private FileHandle info;
	private static final Texture shadow = new Texture("itemShadow.png");
	
	public Item(Type type, String name, Texture sprite) {
		super(-1, -1, -1, -1, false, Realms.toRegion(sprite));
		setUp(type, name);
	}
	
	public Item(String name, Items item) {
		super(-1, -1, -1, -1, false, Realms.toRegion(item.texture));
		setUp(Type.GENERIC, name);
		this.item = item;
		addEssence(item.essence, 1);
	}
	
	public Item(Type type, String name, Items item) {
		this(type, name, item.texture);
		setUp(type, name);
		this.item = item;
	}
	
	public Item() {
		this(Type.WEAPON, "", Items.UNKNOWN.texture);
		this.sizeMax = 1;
	}
	
	public void setUp(Type type, String name) {
		this.type = type;
		this.name = name;
		this.id = name;
		size = 1;
		sizeMax = type.sizeMax;
		velocity = 0;
		essences = new ArrayList<Essence>();
		scripts = new ArrayList<FileHandle>();
	}
	
	public enum Type {
		WEAPON(1),
		GEAR(1),
		GENERIC(24),
		;
		
		public final int sizeMax;
		
		private Type(int sizeMax) {
			this.sizeMax = sizeMax;
		}
	}
	
	public enum Items {
		UNKNOWN("item/unknown.png", Color.WHITE),
		PAPER("item/paper.png", Color.WHITE),
		BONE("item/bone.png", Color.WHITE, Essences.UNDEAD),
		GUTS("item/guts.png", Color.DARK_GRAY, Essences.UNDEAD),
		
		PLASMA_BOX("item/gear/plasma_box.png", Color.GOLD),
		SKULL("item/weapon/special/skull.png", Color.LIGHT_GRAY),
		HOLY_WATER("item/gear/holy_water.png", Color.ROYAL),
		FLY_TRAP("item/gear/fly_trap.png", Color.GREEN),
		SNAKE_SKIN("item/weapon/special/snake_skin.png", Color.TAN),
		NEODYMIUM_GLOVE("item/gear/neodymium_glove.png", Color.GRAY),
		
		RUSTED_GOLD("item/gear/rusted_gold.png", Color.GOLD),
		WRATH_DASHER("item/gear/wrath_dasher.png", Color.FIREBRICK),
		PILLOW("item/gear/pillow.png", Color.LIGHT_GRAY),
		INFINITY_POTION("item/gear/infinity_potion.png", Color.PINK),
		EMERALD("item/gear/emerald.png", Color.GREEN),
		LIPSTICK("item/gear/lipstick.png", Color.MAGENTA),
		TROPHY("item/gear/trophy.png", Color.GOLDENROD),
		
		BOOTS("item/gear/boots.png", Color.TAN),
		CLOAK("item/gear/cloak/cloak.png", Color.OLIVE),
		CLOAK_RED("item/gear/cloak/cloak_red.png", Color.RED),
		CLOAK_PURPLE("item/gear/cloak/cloak_purple.png", Color.PURPLE),
		CLOAK_BROWN("item/gear/cloak/cloak_brown.png", Color.BROWN),
		LINK("item/gear/magiclink.png", Color.SKY),
		REFLECTOR("item/gear/reflector.png", Color.LIGHT_GRAY),
		SPIKE_HELMET("item/gear/spikehelmet/spikehelmet.png", Color.RED),
		SPIKE_HELMET_YELLOW("item/gear/spikehelmet/spikehelmet_yellow.png", Color.YELLOW),
		SPIKE_HELMET_BLUE("item/gear/spikehelmet/spikehelmet_blue.png", Color.BLUE),
		SPIKE_HELMET_GRAY("item/gear/spikehelmet/spikehelmet_gray.png", Color.GRAY),
		TORCH("item/gear/torch/torch.png", Color.ORANGE),
		TORCH_RED("item/gear/torch/torch_red.png", Color.RED),
		TORCH_BLUE("item/gear/torch/torch_blue.png", Color.ROYAL),
		TORCH_GREEN("item/gear/torch/torch_green.png", Color.CHARTREUSE),
		SCROLL("item/gear/scroll.png", Color.WHITE),
		ROCKET_BOOTS("item/gear/rocket_boots.png", Color.GRAY),
		MAGNET("item/gear/magnet/magnet.png", Color.RED),
		MAGNET_BLUE("item/gear/magnet/magnet_blue.png", Color.ROYAL),
		MAGNET_GRAY("item/gear/magnet/magnet_gray.png", Color.LIGHT_GRAY),
		MAGNET_BLACK("item/gear/magnet/magnet_black.png", Color.DARK_GRAY),
		SCOPE("item/gear/scope.png", Color.ROYAL),
		FURYBOOTS("item/gear/furyboots.png", Color.RED),
		DIRTYBOOTS("item/gear/dirtyboots.png", Color.BROWN),
		PROXIMITY_SUIT("item/gear/proximity_suit.png", Color.WHITE),
		BLOOD_AMULET("item/gear/blood_amulet.png", Color.FIREBRICK),
		POWERBALL("item/gear/powerball.png", Color.ORANGE),
		HOURGLASS("item/gear/hourglass.png", Color.CYAN),
		MEDICINE("item/gear/medicine.png", Color.MAROON),
		POISON_AMULET("item/gear/poison_amulet.png", Color.GREEN),
		WIRE_CUTTERS("item/gear/wire_cutters.png", Color.SCARLET),
		DIFFUSER("item/gear/diffuser.png", Color.ROYAL),
		ELECTRIC_AMULET("item/gear/electric_amulet.png", Color.YELLOW),
		CHEST_TOY("item/gear/chest.png", Color.TAN),
		FROZEN_RING("item/gear/frozen_ring.png", Color.CYAN),
		FIRE_AMULET("item/gear/fire_amulet.png", Color.RED),
		NULLZONE_RING("item/gear/nullzone_ring.png", Color.MAGENTA),
		
		BOOMERANG("item/weapon/boomerang.png", Color.ROYAL),
		FORK("item/weapon/special/fork.png", Color.LIGHT_GRAY),
		BLOOD_BOW("item/weapon/special/blood_bow.png", Color.MAROON),
		CHILLED_RECURVE("item/weapon/special/chilled_recurve.png", Color.CYAN),
		COBBLED_SWORD("item/weapon/special/cobbled_sword.png", Color.LIGHT_GRAY, Direction.RANDOM),
		DEEPWATER_TRIDENT("item/weapon/special/deepwater_trident.png", Color.ROYAL, Direction.RANDOM) {
			public boolean stab() { 
				return true; 
			}
		},
		PIRANHIC_TRIDENT("item/weapon/special/piranhic_trident.png", Color.CYAN, Direction.RANDOM) {
			public boolean stab() { 
				return true; 
			}
		},
		DARKBLOOD_TRIDENT("item/weapon/special/darkblood_trident.png", Color.FIREBRICK, Direction.RANDOM) {
			public boolean stab() { 
				return true; 
			}
		},
		EVIL_SICKLE("item/weapon/special/evil_sickle.png", Color.PURPLE, Direction.COUNTER_CLOCKWISE),
		IGNITION_SABRE("item/weapon/special/ignition_sabre.png", Color.SCARLET, Direction.RANDOM),
		REAPING_SCYTHE("item/weapon/special/reaping_scythe.png", Color.FIREBRICK, Direction.CLOCKWISE),
		VENOM_JAVELIN("item/weapon/special/venom_javelin.png", new Color(81/255f, 1f, 81/255f, 1f)),
		VENOM_KNIFE("item/weapon/special/venom_knife.png", new Color(81/255f, 1f, 81/255f, 1f)),
		TORPEDO_DAGGER("item/weapon/special/torpedo_dagger.png", Color.TEAL),
		TORPEDO_LANCE("item/weapon/special/torpedo_lance.png", Color.TEAL),
		PHANTOLANCE("item/weapon/special/phantolance.png", new Color(204/255f, 1f, 204/255f, 1f)),
		GRAVEDIGGER("item/weapon/special/gravedigger.png", Color.SLATE, Direction.RANDOM),
		GRAVEDIGGER_FIRE("item/weapon/special/gravedigger_fire.png", Color.RED, Direction.RANDOM),
		DESERT_STAFF("item/weapon/special/desert_staff.png", Color.TAN, Direction.RANDOM) {
			public boolean doubleEdged() { 
				return true; 
			}
		},
		SEA_SPEAR("item/weapon/special/sea_spear.png", Color.CYAN, Direction.RANDOM) {
			public boolean stab() { 
				return true; 
			}
		},
		BLAZING_BOW("item/weapon/special/blazing_bow.png", Color.SCARLET),
		CLUB("item/weapon/special/club.png", Color.TAN, Direction.RANDOM),
		CLUB_REDWOOD("item/weapon/special/club_redwood.png", Color.SCARLET, Direction.RANDOM),
		CLUB_DARKWOOD("item/weapon/special/club_darkwood.png", Color.GRAY, Direction.RANDOM),
		BONE_KNIFE("item/weapon/special/bone_knife.png", Color.WHITE),
		FROZEN_BLADE("item/weapon/special/frozen_blade.png", Color.CYAN, Direction.RANDOM),
		ELECTRIC_SWORD("item/weapon/special/electric_sword.png", Color.GOLDENROD, Direction.RANDOM),
		DESERT_SPEAR("item/weapon/special/desert_spear.png", Color.TAN),
		TRAVELERS_BOW("item/weapon/special/travelers_bow.png", Color.TAN),
		GOLD_SCIMITAR("item/weapon/special/gold_scimitar.png", Color.GOLD, Direction.RANDOM),
		SILVER_SCIMITAR("item/weapon/special/silver_scimitar.png", Color.LIGHT_GRAY, Direction.RANDOM),
		BRONZE_SCIMITAR("item/weapon/special/bronze_scimitar.png", Color.ORANGE, Direction.RANDOM),
		APOB("item/weapon/special/apob.png", Color.WHITE, Direction.RANDOM),
		METALLIC_TOMAHAWK("item/weapon/special/metallic_tomahawk.png", Color.GRAY) {
			public boolean spin() { 
				return true; 
			}
		},
		MINI_BOW("item/weapon/special/bow.png", Color.TAN),
		STICK("item/weapon/special/stick.png", Color.TAN, Direction.RANDOM),
		STICK_REDWOOD("item/weapon/special/stick_redwood.png", Color.SCARLET, Direction.RANDOM),
		STICK_DARKWOOD("item/weapon/special/stick_darkwood.png", Color.GRAY, Direction.RANDOM),
		BLUEFIRE_STRIKER("item/weapon/special/bluefire_striker.png", Color.PURPLE, Direction.RANDOM),
		DEVILISH_LANCE("item/weapon/special/devilish_lance.png", Color.RED),
		BLADE_OF_BLOOD("item/weapon/special/blade_of_blood.png", Color.RED, Direction.RANDOM),
		BLADE_OF_ENERGY("item/weapon/special/blade_of_energy.png", Color.CHARTREUSE, Direction.RANDOM),
		SLEDGEHAMMER("item/weapon/special/sledgehammer.png", Color.GRAY, Direction.RANDOM),
		ANCIENT_CROSSBOW("item/weapon/special/ancient_crossbow.png", Color.TAN),
		MEGA_CLUB("item/weapon/special/club_mega.png", Color.TAN),
		DAGGER_HOLDER("item/weapon/special/dagger_holder.png", Color.TAN),
		SPLIT_STABBER("item/weapon/special/split_stabber.png", Color.GOLD),
		NEON_SWORD("item/weapon/special/neon_sword.png", Color.CHARTREUSE),
		
		EXTENDED_DAGGER("item/weapon/special/extended_dagger.png", Color.SLATE),
		SKULL_SWORD("item/weapon/special/skull_sword.png", Color.TAN),
		MEDIEVAL_SWORD("item/weapon/special/medieval_sword.png", Color.GRAY),
		LIGHTNING_SWORD("item/weapon/special/lightning_sword.png", Color.YELLOW),
		HEATED_SWORD("item/weapon/special/heated_sword.png", Color.SCARLET),
		INFECTED_SWORD("item/weapon/special/infected_sword.png", Color.GREEN),
		SEISMIC_SCYTHE("item/weapon/special/seismic_scythe.png", Color.GOLD, Direction.CLOCKWISE),
		ANGELIC_SWORD("item/weapon/special/angelic_sword.png", Color.WHITE),
		DEVIL_SWORD("item/weapon/special/devil_sword.png", Color.RED),
		GRAVEYARD_REAPER("item/weapon/special/graveyard_reaper.png", Color.OLIVE, Direction.COUNTER_CLOCKWISE),
		MOONLIGHT_REAPER("item/weapon/special/moonlight_reaper.png", Color.BLUE, Direction.COUNTER_CLOCKWISE),
		DIRTY_SWORD("item/weapon/special/dirty_sword.png", Color.BROWN),
		GUARD_AXE("item/weapon/special/guard_axe.png", Color.SLATE),
		BLOODY_BATTLEAXE("item/weapon/special/bloody_battleaxe.png", Color.RED),
		PULSAR_SWORD("item/weapon/special/pulsar_sword.png", Color.CYAN),
		DARKLIGHT_SWORD("item/weapon/special/darklight_sword.png", Color.SLATE),
		FIGHTING_SWORD("item/weapon/special/fighting_sword.png", Color.LIGHT_GRAY),
		NETHER_HALBERD("item/weapon/special/nether_halberd.png", Color.MAROON),
		DARKBLOOD_MACE("item/weapon/special/darkblood_mace.png", Color.RED, Direction.CLOCKWISE),
		FIGHTING_DAGGER("item/weapon/special/fighting_dagger.png", Color.LIGHT_GRAY),
		DIAMOND_DAGGER("item/weapon/special/diamond_dagger.png", Color.CYAN),
		WARRIOR_BLADE("item/weapon/special/warrior_blade.png", Color.LIGHT_GRAY),
		WARRIOR_BLADE_BLOODY("item/weapon/special/warrior_blade_bloody.png", Color.FIREBRICK),
		KING_BLADE("item/weapon/special/king_blade.png", Color.PURPLE),
		BATTLE_BOW("item/weapon/special/battle_bow.png", Color.GRAY),
		FIGHTING_SPEAR("item/weapon/special/fighting_spear.png", Color.LIGHT_GRAY),
		FIGHTING_AXE("item/weapon/special/fighting_axe.png", Color.LIGHT_GRAY),
		VENOM_BOW("item/weapon/special/venom_bow.png", Color.CHARTREUSE),
		BLUNT_CROSSBOW("item/weapon/special/blunt_crossbow.png", Color.GRAY),
		SPECTER_DAGGER("item/weapon/special/specter_dagger.png", Color.PURPLE),
		ANGELIC_BOW("item/weapon/special/angelic_crossbow.png", Color.WHITE),
		NETHER_BOW("item/weapon/special/red_bow.png", Color.MAROON),
		SILHOUETTE_BOW("item/weapon/special/silhouette_bow.png", Color.DARK_GRAY),
		LUMBER_AXE("item/weapon/special/lumber_axe.png", Color.TAN, Direction.COUNTER_CLOCKWISE),
		SHURIKEN("item/weapon/special/shuriken.png", Color.LIGHT_GRAY) {
			public boolean spin() { 
				return true; 
			}
		},
		RED_FANG("item/weapon/special/red_fang.png", Color.FIREBRICK),
		BLOOD_SWORD_ULTIMATE("item/weapon/special/blood_sword_ultimate.png", Color.RED),
		
		
		PHOENIX_SHIELD("item/weapon/special/phoenix_shield.png", Color.ORANGE),

		BOOK("item/book.png", Color.WHITE),
		;
		
		public final Texture texture;
		public final Color color;
		public final Direction direction;
		public final Essences essence;
		
		private Items(String directory, Color color, Direction direction, Essences essence) {
			texture = new Texture(directory);
			this.color = color;
			this.direction = direction;
			this.essence = essence;
		}
		
		private Items(String directory, Color color, Direction direction) {
			this(directory, color, direction, null);
		}
		
		private Items(String directory, Color color, Essences essence) {
			this(directory, color, Direction.RANDOM, essence);
		}
		
		private Items(String directory, Color color) {
			this(directory, color, Direction.RANDOM, null);
		}
		
		public boolean spin() { return false; }
		
		public boolean stab() { return false; }
		
		public boolean doubleEdged() { return false; }
		
	}
	
	
	public void render() { }
	
	public void use(float xDir, float yDir) { }
	
	public void pickupBy(Creature creature) {
		if (creature instanceof Player) {
			size = Realms.getGame().gui().inventory().add(this, true);
			if (size == 0) {
				setShouldDelete(true);
			}
		} else {
			setShouldDelete(true);
			if (this instanceof Heart) {
				((Heart)this).getHeartType().pickupByCreature(creature, ((Heart)this).getValue(), this);
			} else {
				if (this instanceof Weaponizable) {
					((CombatGear)this).setCooldown(Math.max(((CombatGear)this).getCooldown(), 20));
				}
				creature.addLoot(new Loot(this));
				Audio.play(Sounds.PICKUP, centerX());
			}
		}
	}
	
	
	public void draw(int x, int y, int originX, int originY, int width, int height, boolean inGUI, boolean spin, float a) {
		Graphics.begin();
		Graphics.setColor(Color.WHITE, !inGUI);
		if (!inGUI && !(this instanceof Heart && ((Heart)this).getHeartType() == Heart.Type.HEALTH_MINI) && Gdx.graphics.getFramesPerSecond() > 50) {
			Graphics.draw(shadow, x - 25, y - 25);
		}
		float rotation = this == Realms.getGame().gui().getInfoItem() && Realms.getGame().getWindow() == Window.BACKPACK ? (float)Math.cos((double)life/20) : 1;
		if (this instanceof CombatGear && ((CombatGear)(this)).texture() != null) {
			((CombatGear)(this)).texture().draw(x, y, originX, originY, width, height, spin ? rotation : 1, 1, 0, a, !inGUI);
		} else {
			Graphics.setA(a);
			Graphics.draw(getSprite(), x, y, originX, originY, width, height, spin ? rotation : 1, 1, 0);
		}
		Graphics.setColor(Color.WHITE);
		Graphics.end();
		if (Realms.getGame().getBounds() && !inGUI) {
			Graphics.shapeRenderer.begin(ShapeType.Line);
			Graphics.rect(getX(), getY(), getWidth(), getHeight());
			Graphics.shapeRenderer.end();
		}
	}
	
	public void draw(int x, int y, boolean inGUI, boolean spin, float a) {
		draw(x, y, 25, 25, 50, 50, inGUI, spin, a);
	}
	
	public void draw(boolean frozen) {
		draw(getX(), getY(), false, false, 1f);
		if (pickupDelay > 0 && !frozen) {
			pickupDelay--;
		}
	}
	
	public void drawCooldown(int x, int y, float a) {
		if ((this instanceof Weaponizable || this instanceof Activatable) && !(this instanceof Material)) {
			int cooldownMax = ((CombatGear)this).getCooldownMax();
			if (((CombatGear)this).getCooldown() > 0 && cooldownMax > 0) {
				Graphics.begin();
				Graphics.setColor(1, 1, 1, a, true);
				Graphics.draw(new TextureRegion(Slot.Type.slots(), 250, 50 - 50*((CombatGear)this).getCooldown()/cooldownMax, 50, 50*((CombatGear)this).getCooldown()/cooldownMax), x, y);
				Graphics.resetColor();
				Graphics.end();
			}
		}
	}
	
	public void drawWindup(int x, int y, float a) {
		if (this instanceof WindupWeapon) {
			int windup = (int)((WindupWeapon)this).getWindup();
			if (windup > 0) {
				Graphics.begin();
				Graphics.setColor(new Color(1f, .3f, .3f, .8f*a));
				Graphics.draw(new TextureRegion(Slot.Type.slots(), 250, 50 - 50*windup/((WindupWeapon)this).getWindupMin(), 50, 50*windup/((WindupWeapon)this).getWindupMin()), x, y);
				Graphics.resetColor();
				Graphics.end();
			}
		}
		if (this instanceof ShieldActive) {
			int meter = (int)((ShieldActive)this).getMeter();
			float gb = ((ShieldActive)this).getOverheated() ? .1f : .3f;
			if (meter > 0) {
				Graphics.setColor(new Color(1f, gb, gb, .8f*a));
				Graphics.begin();
				Graphics.draw(new TextureRegion(Slot.Type.slots(), 250, 50 - 50*meter/((ShieldActive)this).getMeterMax(), 50, 50*meter/((ShieldActive)this).getMeterMax()), x, y);
				Graphics.end();
				Graphics.resetColor();
			}
		}
		if (this instanceof Link) {
			int holdingTime = (int)((Link)this).getHoldingTime();
			if (holdingTime > 0) {
				Graphics.setColor(new Color(1f, .3f, .3f, .8f*a));
				Graphics.begin();
				if (((Link)this).isLinked()) {
					Graphics.draw(new TextureRegion(Slot.Type.slots(), 250, 50 - 50*holdingTime/Link.HOLD_TIME_TP, 50, 50*holdingTime/Link.HOLD_TIME_TP), x, y);
				}
				Graphics.draw(new TextureRegion(Slot.Type.slots(), 250, 50 - 50*holdingTime/Link.HOLD_TIME_SET, 50, 50*holdingTime/Link.HOLD_TIME_SET), x, y);
				Graphics.end();
				Graphics.resetColor();
			}
		}
	}
	
	public void drawStack(int x, int y, float a) {
		if (sizeMax > 1) {
			Text stack = new Text(Integer.toString(size), size == sizeMax ? Color.LIGHT_GRAY : Color.WHITE, 20);
			stack.setAlpha(a);
			stack.render(true, x + 46 - stack.getWidth(), y + 3);
		}
	}
	
	public Item addEssence(Essences newEssence, int potency) {
		if (newEssence != null) {
			boolean found = false;
			for (Essence essence : essences) {
				if (essence.getEssence() == newEssence) {
					essence.augment(potency);
					found = true;
				}
			}
			if (!found) {
				essences.add(new Essence(newEssence, potency));
			}
		}
		return this;
	}
	
	public void addEssence(ArrayList<Essence> essences) {
		for (Essence essence : essences) {
			addEssence(essence.getEssence(), essence.getPotency());
		}
	}
	
	public static Item makeActiveItem(Item item, int x, int y, int count, int pickupDelay) {
		item = item.clone();
		item.setWidth(50);
		item.setHeight(50);
		item.setX(x);
		item.setY(y);
		item.setPickupDelay(pickupDelay);
		if (item.getLight() != null) {
			item.getLight().setSource(item);
		}
		return item;
	}
	
	public static void drop(Item item, int x, int y, int xVar, int yVar, int count, int pickupDelay, Room room) {//TODO
		item = item.clone();
		item.setWidth(50);
		item.setHeight(50);
		item.setSize(count);
		int attempts = 0;
		do {
			item.setX(Realms.vary(x, xVar));
			item.setY(Realms.vary(y, yVar));
			if (++attempts > 100) {
				attempts = 0;
				xVar += 10;
				yVar += 10;
			}
		} while (!item.getPermeable() && item.entering(room.solidBlocks()) || item.outOfBounds());
		item.setPickupDelay(pickupDelay);
		if (item.getLight() != null) {
			item.getLight().setSource(item);
		}
		item.setShouldDelete(false);
		room.addItem(item);
	}
	
	public static void drop(Item item, int x, int y, int xVar, int yVar, int count, int pickupDelay) {
		drop(item, x, y, xVar, yVar, count, pickupDelay, Realms.getGame().getRoom());
	}
	
	public static void incrementLife() {
		life++;
	}
	
	public static void resetLife() {
		life = 0;
	}
	
	@SuppressWarnings("unchecked")
	public Item clone() {
		Item item;
		item = (Item)super.clone();
	    item.setScripts((ArrayList<FileHandle>)scripts.clone());
	    if (Realms.getGame().gui().getInfoItem() == this) {
	    	Realms.getGame().gui().setInfoItem(item);
	    }
	    if (getLight() != null) {
	    	setLight(new Light(getLight().getRadiusA(), getLight().getRadiusB(), getLight().getColor(), this));
	    }
	    return item;
    }
	
	public boolean equals(Item item) {
		if (item != null && name.equals(item.name)) {
			if (this instanceof CombatGear && item instanceof CombatGear) {
				return ((CombatGear)this).equals((CombatGear)item);
			} else {
				return true;
			}
		}
		return false;
	}
	
	public static boolean equals(Item item1, Item item2) {
		if (item1 == null && item2 == null) {
			return true;
		} else if (item1 != null && item2 != null) {
			return item1.equals(item2);
		} else if (item1 == null) {
			return item2.equals(item1);
		} else {
			return item1.equals(item2);
		}
	}
	
	public boolean isWeapon() {
		return this instanceof CombatGear && getType() == Type.WEAPON;
	}
	
	public boolean testHandicap(int test) {
		return Realms.getGame().getRoom().testHandicap(this, test);
	}
	
//	public boolean hasStats() {
//		return this instanceof CombatGear && ((CombatGear)this).stats() != null;
//	}
	
	public static boolean equalType(Item item1, Item item2) {
		return item1 != null && item2 != null && 
				(item1.getClass().equals(item2.getClass()) || (item1.getClass().isAnonymousClass() && item2.getClass().isAssignableFrom(item1.getClass())) || (item2.getClass().isAnonymousClass() && item1.getClass().isAssignableFrom(item2.getClass())));
	}
	
	public boolean keyPressed() {
		return owner instanceof Player && (Gdx.input.isKeyPressed((int)Option.BIND_ATTACK_DOWN.button.getState()) || Gdx.input.isKeyPressed((int)Option.BIND_ATTACK_LEFT.button.getState()) || Gdx.input.isKeyPressed((int)Option.BIND_ATTACK_RIGHT.button.getState()) || Gdx.input.isKeyPressed((int)Option.BIND_ATTACK_UP.button.getState()));
	}
	
	public Item get(int floor) {
		return this;
	}
	
	public Type getType() {
		return type;
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public int getSizeMax() {
		return sizeMax;
	}
	
	public void setSizeMax(int sizeMax) {
		this.sizeMax = sizeMax;
	}
	
	public void stack() {
		size++;
	}
	
	public void unstack() {
		size--;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public Item setId(String id) {
		this.id = id;
		return this;
	}
	
	public void setPickupDelay(int pickupDelay) {
		this.pickupDelay = pickupDelay;
	}
	
	public int getPickupDelay() {
		return pickupDelay;
	}
	
	public Item ammendScript(String file) {
		scripts.add(Gdx.files.internal("texts/scripts/" + file));
		return this;
	}
	
	public void addScript(FileHandle fh) {
		scripts.add(fh);
	}
	
	public ArrayList<FileHandle> getScripts() {
		return scripts;
	}
	
	public void setScripts(ArrayList<FileHandle> scripts) {
		this.scripts = scripts;
	}
	
	public int getScriptIndex() {
		return scriptIndex;
	}
	
	public void setScriptIndex(int scriptIndex) {
		this.scriptIndex = scriptIndex;
	}
	
	public void addScriptIndex(int amt) {
		scriptIndex += amt;
	}
	
	public FileHandle currentScript() {
		return scripts.get(scriptIndex);
	}
	
	public Item setInfo(String file) {
		info = Gdx.files.internal("texts/info/" + file);
		return this;
	}
	
	public FileHandle getInfo() {
		return info;
	}
	
	public Creature getOwner() {
		return owner;
	}
	
	public void setOwner(Creature owner) {
		this.owner = owner;
	}
	
	public boolean getOld() {
		return old;
	}
	
	public void setOld(boolean old) {
		this.old = old;
	}
	
	public boolean getStarter() {
		return starter;
	}
	
	public void setStarter(boolean starter) {
		this.starter = starter;
	}
	
	public Items getItem() {
		return item;
	}
	
	public void setItem(Items item) {
		this.item = item;
	}
	
	public int getVelocity() {
		return velocity;
	}
	
	public void setVelocity(int velocity) {
		this.velocity = velocity;
	}

	public void changeVelocity(int amt) {
		velocity += amt;
	}
	
	public ArrayList<Essence> getEssences() {
		return essences;
	}
	
	public Item setLight(Light light) {
		super.setLight(light);
		return (Item)this;
	}
	
	public boolean getPermeable() {
		return permeable;
	}
	
	public void setPermeable(boolean permeable) {
		this.permeable = permeable;
	}
	
	
	public Item n() {
		old = true;
		starter = true;
		return this;
	}
	
	public Item wrap() {
		return this;
	}

	public Color nameColor() {
		if (this instanceof CombatGear && ((CombatGear)this).texture() != null) {
			return ((CombatGear)this).texture().getColor();
		}
		if (this instanceof Material) {
			return ((Material)this).getMaterial().color;
		}
		if (this instanceof Consumable) {
			return ((Consumable)this).getColor().color;
		}
		if (this instanceof Scroll && ((Scroll)this).getMOType() != null) {
			return ((Scroll)this).getMOType().color;
		}
		if (this instanceof Bomb) {
			return ((Bomb)this).getBomb().color;
		}
		if (this instanceof Badge) {
			return ((Badge)this).getBadgeType().color;
		}
		if (this instanceof Key) {
			return ((Key)this).getKeyType().color;
		}
		if (item != null) {
			return item.color;
		}
		return Color.WHITE;
	}
	
}
