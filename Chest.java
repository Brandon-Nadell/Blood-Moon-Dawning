package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.Color;

public class Chest extends LockedBlock {
	
	private Item item;
	private boolean open;
	private CombatGear explosion;
	private int timer;
	private static final Creature GAME = new Creature(MovingObjects.BOX, Group.NEUTRAL, 0, 0, 0, 0, 0, null);
	
	public Chest(int x, int y, int width, int height, Blocks sprite, Item item, Keys key, boolean solid) {
		super(x, y, width, height, sprite, key);
		this.item = item;
		timer = -1;
		setSolid(solid);
		setShadows(.4f);
	}
	
	public Chest(int x, int y, int width, int height, Blocks sprite, Item item, Keys key) {
		this(x, y, width, height, sprite, item, key, false);
	}
	
	public Chest(int x, int y, int width, int height, Blocks sprite, Item item, boolean solid) {
		this(x, y, width, height, sprite, item, null, solid);
	}
	
	public Chest(int x, int y, int width, int height, Blocks sprite, Item item) {
		this(x, y, width, height, sprite, item, null, false);
	}
	
	public Chest(int x, int y, int width, int height, Blocks sprite) {
		this(x, y, width, height, sprite, null, null, false);
	}
	
	public Chest(int x, int y, int width, int height, Subtype subtype) {
		this(x, y, width, height, null, null, null, false);
		setSubtype(subtype);
	}
	
	public Chest(Block block, Item item) {
		this(block.getX(), block.getY(), block.getWidth(), block.getHeight(), null, item, null, false);
		setSprite(block.getSprite());
		setSolid(true);
		clearShadows();
	}
	
	public void forceOpenInRoom(Room room) {
		unlock();
		drop(room);
		trigger();
		if (room == Realms.getGame().getRoom()) {
			if (getType().animations.length > 0) {
				setAnimation(0);
			}
		} else if (getType().animations.length > 0) {
			endAnimation(0);
		}
		Audio.play(Sounds.CHEST, 0.5f, 0.75f, centerX());
	}
	
	public void open() {
		if (!getOpen() && (item != null || explosion != null) && notBlocked()) {
			if (Block.openable(getKey(), true, centerX()) && getType().breaks.length == 0) {
				unlock();
				drop(null);
				trigger();
				if (getType().animations.length > 0) {
					setAnimation(0);
				}
				Audio.play(Sounds.CHEST, 0.5f, 0.75f, centerX());
			} else if (checkMessageDelayOver()) {
				Realms.getGame().getNotifs().add("> Chest is " + (getType().breaks.length == 0 ? "locked" : getType().breaks[0].chestMessage) + "!");
				shake();
			}
		}
	}
	
	public void unlock() {
		setOpen(true);
	}
	
	public void drop(Room room) {
		if (item != null) {
			item.setPermeable(true);
			Item.drop(item, centerX() - 25, centerY() - 25, 1, 1, 1, 60, room == null ? Realms.getGame().getRoom() : room);
		}
	}
	
	public void trigger() {
		if (explosion != null) {
			if (explosion instanceof Bomb) {
				Realms.getGame().getRoom().getProjectiles().remove(((Bomb)explosion).explode(centerX(), centerY()));
			}
			if (explosion instanceof Consumable) {
				((Consumable)explosion).splash(centerX(), centerY());
			}
			destroy();
		}
	}
	
	public boolean notBlocked() {
		for (Block block : Realms.getGame().getRoom().solidBlocks()) {
			if (entering(block)) {
				return false;
			}
		}
		return true;
	}
	
	public void shake() {
		timer = 0;
	}
	
	public void animate(boolean frozen) {
		if (!frozen) {
			if (timer >= 0 && timer <= 24) {
				if (timer % 12 == 0) {
					Audio.play(Sounds.LOCKED, .6f, 1f, centerX());
					setRotation(0);
				} else if (timer % 12 == 3) {
					setRotation(4);
				} else if (timer % 12 == 6) {
					setRotation(0);
				} else if (timer % 12 == 9) {
					setRotation(-4);
				}
				timer++;
			}
		} else {
			setRotation(0);
			timer = -1;
		}
	}
	
	public void draw(boolean frozen) {
		super.draw(frozen);
		drawItem();
		animate(frozen);
	}
	
	public void drawItem() {
		 if (!open && Realms.getGame().getPlayer().getClairvoyant()) {
			if (item != null) {
				Graphics.setColor(new Color(1f, 1f, 1f, 0f), true);
				item.draw(centerX() - 25, centerY() - 25, 0, 0, 50, 50, false, false, .625f);
				Graphics.resetColor();
			}
			if (explosion != null) {
//				Projectile.drawAreaIndicator(MovingObjects.BOMB, explosion, centerX(), centerY(), .5f);
				Word danger = new Word("!", 0, 0, new Color(1f, 0f, 0f, .5f), 60, Format.DEFAULT);
				danger.render(centerX() - danger.getWidth()/2, centerY() - 30);
			}
		}
	}
	
	public Chest clone() {
		Chest chest = (Chest)new Chest(getX(), getY(), getWidth(), getHeight(), getType(), item, getKey(), getSolid()).setSubtype(getSubtype());
		chest.setSprite(getSprite());
		if (!hasShadows()) {
			chest.clearShadows();
		}
		return chest;
	}
	
	public Item getItem() {
		return item;
	}
	
	public void setItem(Item item) {
		this.item = item;
	}
	
	public Chest setExplosion(CombatGear explosion) {
		this.explosion = explosion;
		explosion.setOwner(GAME);
		return this;
	}

}
