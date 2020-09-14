package com.mygdx.game.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Audio;
import com.mygdx.game.Graphics;
import com.mygdx.game.Realms;
import com.mygdx.game.Statistics.StatType;
import com.mygdx.game.Statistics.Attribute.Special;
import com.mygdx.game.TintedTexture.Model;
import com.mygdx.game.Audio.Sounds;
import com.mygdx.game.Function;
import com.mygdx.game.effects.Light;
import com.mygdx.game.effects.Light.LightColor;
import com.mygdx.game.effects.Particle;
import com.mygdx.game.entity.Block.Blocks.Animation;
import com.mygdx.game.entity.Block.Blocks.Subtype;
import com.mygdx.game.entity.MovingObject.Group;
import com.mygdx.game.item.Key.Keys;
import com.mygdx.game.item.gear.CombatGear;
import com.mygdx.game.item.weapon.Crossbow;
import com.mygdx.game.item.weapon.Crossbow.Bolt;
import com.mygdx.game.item.weapon.Sword;

public class Block extends Entity {
	
	private boolean solid;
	private int rotation;
	private MoveCommand move;
	private Blocks type;
	private Animation animation;
	private Subtype subtype;
	private Color tint;
	private Entity[][] shadows;
	private boolean shadowed;
	private float shadowA;
	private ArrayList<TextureRegion> cracks;
	private static ArrayList<TextureRegion> cracksDefault;
	private static final TextureRegion shadow70x = Realms.toRegion(new Texture("shadowChest.png"));
	private static final TextureRegion shadow80x = Realms.toRegion(new Texture("shadowCrate.png"));
	private static final Texture shadow = new Texture("shadows.png");
	
	static {
		cracksDefault = new ArrayList<TextureRegion>();
		Texture crack = new Texture("entity/block/cracks.png");
		for (int i = 0; i < crack.getWidth(); i += 80) {
			cracksDefault.add(new TextureRegion(crack, i, 0, 80, 80));
		}
	}
	
	public Block(float x, float y, int width, int height, boolean circular, Blocks block, Light light) {
		super(x, y, width, height, circular, block == null ? null : Realms.toRegion(block.texture));
		this.solid = true;
		this.type = block;
		cracks = new ArrayList<TextureRegion>();
		tint = new Color(Color.WHITE);
		if (light != null) {
			light.setSource(this);
			setLight(light);
		}
	}
	
	public Block(float x, float y, int width, int height, boolean circular, Blocks block) {
		this(x, y, width, height, circular, block, null);
	}
	
	public Block(float x, float y, int width, int height, Blocks block, Light light) {
		this(x, y, width, height, false, block, light);
	}
	
	public Block(float x, float y, int width, int height, Blocks block, TextureRegion sprite) {
		this(x, y, width, height, false, block);
		setSprite(sprite);
	}
	
	public Block(float x, float y, int width, int height, boolean circular) {
		this(x, y, width, height, circular, null, null);
	}
	
	public Block(float x, float y, int width, int height, Blocks block) {
		this(x, y, width, height, false, block, null);
	}
	
	public Block(float x, float y, int width, int height) {
		this(x, y, width, height, false, null, null);
	}
	
	public Block(int x, int y, Blocks block, Light light) {
		this(x, y, block.texture.getWidth(), block.texture.getHeight(), false, block, null);
		this.solid = false;
		light.setSource(this);
		setLight(light);
	}

	public enum Blocks {
		WOOD(new Texture("entity/block/material/wood.png"), 0, Color.TAN, Break.AXE),
		WOOD_LIGHT(new Texture("entity/block/material/wood_light.png"), 0, Color.GOLD, Break.AXE),
		WOOD_DARK(new Texture("entity/block/material/wood_dark.png"), 0, Color.BROWN, Break.AXE),
		WOOD_RED(new Texture("entity/block/material/wood_red.png"), 0, Color.SCARLET, Break.AXE),
		WOOD_WHITE(new Texture("entity/block/material/wood_white.png"), 0, Color.WHITE, Break.AXE),
		WOOD_BLACK(new Texture("entity/block/material/wood_black.png"), 0, Color.DARK_GRAY, Break.AXE),
		WOOD_HARD(new Texture("entity/block/material/wood.png"), 0, Color.TAN),
		WOOD_DARK_HARD(new Texture("entity/block/material/wood_dark.png"), 0, Color.BROWN),
		
		CAVEROCK(new Texture("entity/block/material/caverock.png"), .4, Color.GRAY, Break.EXPLODE),
		CAVEROCK_BLACK(new Texture("entity/block/material/caverock_black.png"), .4, Color.DARK_GRAY, Break.EXPLODE),
		CAVEROCK_RED(new Texture("entity/block/material/caverock_red.png"), .4, Color.FIREBRICK, Break.EXPLODE),
		CAVEROCK_DENT(new Texture("entity/block/material/caverock_dent.png"), .4, Color.DARK_GRAY, Break.EXPLODE),
		ROCK_BRICK(new Texture("entity/block/material/rock_brick.png"), .3),
		ROCK_BRICK_DARK(new Texture("entity/block/material/rock_brick_dark.png"), .3),
		ROCK_BRICK_RED(new Texture("entity/block/material/rock_brick_red.png"), .3),
		PACKED_COBBLE_BLACK(new Texture("entity/block/material/packed_cobble.png"), .4),
		PACKED_COBBLE_DARK_RED(new Texture("entity/block/material/packed_cobble_dark_red.png"), .4),
		PACKED_COBBLE_RED(new Texture("entity/block/material/packed_cobble_red.png"), .4),
		STONE(new Texture("entity/block/material/stone.png"), .3),
		REDROCK(new Texture("entity/block/material/redrock.png"), .3, Color.GRAY, Break.EXPLODE),
		
		GRASS(new Texture("entity/block/material/grass.png"), 0),
		VOID(new Texture("entity/block/material/void.png"), 0),
		SPOOKY(new Texture("entity/block/material/spooky.png"), 0),
		SURFACE_1(new Texture("entity/block/material/surface1.png"), 0),
		SURFACE_2(new Texture("entity/block/material/surface2.png"), 0),
		SURFACE_3(new Texture("entity/block/material/surface3.png"), 0),
		SURFACE_4(new Texture("entity/block/material/surface4.png"), 0),
		SURFACE_5(new Texture("entity/block/material/surface5.png"), 0),
		SURFACE_BRICK_1(new Texture("entity/block/material/surface_brick1.png"), 0),
		SURFACE_BRICK_2(new Texture("entity/block/material/surface_brick2.png"), 0),
		SURFACE_BRICK_3(new Texture("entity/block/material/surface_brick3.png"), 0),
		SURFACE_BRICK_4(new Texture("entity/block/material/surface_brick4.png"), 0),
		SURFACE_BRICK_5(new Texture("entity/block/material/surface_brick5.png"), 0),
		
		METAL(new Texture("entity/block/material/metal.png"), .5),
		METAL_2(new Texture("entity/block/material/metal2.png"), .5),
		METAL_3(new Texture("entity/block/material/metal3.png"), .5),
		METAL_4(new Texture("entity/block/material/metal4.png"), .5),
		METAL_5(new Texture("entity/block/material/metal5.png"), .5),
		ICE_SOLID(new Texture("entity/block/material/ice.png"), 0, Color.ROYAL, Break.FIRE),
		SPIKE(new Texture("entity/block/material/spike.png"), 0),
		SPIKE_S(new Texture("entity/block/material/spikeS.png"), 0),
		
		BRICK_WALL(new Texture("entity/block/brick_wall.png"), 0),

		CHEST_WOOD(new Texture("entity/block/chest/chest_wood.png"), 0, Animation.CHEST_WOOD_OPEN),
		CHEST_WOOD_LIGHT(new Texture("entity/block/chest/chest_wood_light.png"), 0, Animation.CHEST_WOOD_LIGHT_OPEN),
		CHEST_WOOD_DARK(new Texture("entity/block/chest/chest_wood_dark.png"), 0, Animation.CHEST_WOOD_DARK_OPEN),
		CHEST_WOOD_RED(new Texture("entity/block/chest/chest_wood_red.png"), 0, Animation.CHEST_WOOD_RED_OPEN),
		CHEST_WOOD_WHITE(new Texture("entity/block/chest/chest_wood_white.png"), 0, Animation.CHEST_WOOD_WHITE_OPEN),
		CHEST_WOOD_BLACK(new Texture("entity/block/chest/chest_wood_black.png"), 0, Animation.CHEST_WOOD_BLACK_OPEN),
		CHEST_MAGIC(new Texture("entity/block/chest/chest_magic.png"), 0, null, new Animation[] { Animation.CHEST_MAGIC_OPEN }, new Break[] { Break.MAGIC }),
		CHEST_EYES(new Texture("entity/block/chest/chest_eyes.png"), 0, Animation.CHEST_EYES_OPEN, Animation.CHEST_EYES_CLOSE),
		CHEST_STONE(new Texture("entity/block/chest/chest_stone.png"), 0, Color.LIGHT_GRAY, Break.AXE),
		CHEST_METAL(new Texture("entity/block/chest/chest_metal.png"), 0, Color.GRAY, Break.EXPLODE),
		CHEST_LAVA(new Texture("entity/block/chest/chest_lava.png"), 0, Color.RED, Break.ICE),
		CHEST_ICE(new Texture("entity/block/chest/chest_ice.png"), 0, Color.ROYAL, Break.FIRE),
		CHEST_GOLD(new Texture("entity/block/chest/chest_gold.png"), 0, Color.GOLDENROD, Break.AXE),
		CHEST_PACKED_COBBLE(new Texture("entity/block/chest/chest_packed_cobble.png"), 0, Color.GRAY, Break.EXPLODE),
		CRATE(new Texture("entity/block/chest/crate.png"), 0, Color.TAN, Break.AXE),
		CRATE_LIGHT(new Texture("entity/block/chest/crate_light.png"), 0, Color.GOLD, Break.AXE),
		CRATE_DARK(new Texture("entity/block/chest/crate_dark.png"), 0, Color.BROWN, Break.AXE),
		CRATE_RED(new Texture("entity/block/chest/crate_red.png"), 0, Color.SCARLET, Break.AXE),
		CRATE_WHITE(new Texture("entity/block/chest/crate_white.png"), 0, Color.WHITE, Break.AXE),
		CRATE_BLACK(new Texture("entity/block/chest/crate_black.png"), 0, Color.DARK_GRAY, Break.AXE),
		CRATE_COBBLE(new Texture("entity/block/chest/crate_cobble.png"), 0, Color.RED, Break.EXPLODE),
		
		FORGE(new Texture("entity/block/forge.png"), 0),
		COMPRESSOR(new Texture("entity/block/compressor.png"), 0),
		ALTAR(new Texture("entity/block/altar.png"), 0),
		
		DOOR(new Texture("entity/block/door/door_45.png"), 0),
		TRAPDOOR(new Texture("entity/block/door/trapdoor.png"), 0),
		TRAPDOOR_SEALED(new Texture("entity/block/door/trapdoor_sealed.png"), 0),
		LADDER(new Texture("entity/block/door/ladder.png"), 0),
		LADDER_SEALED(new Texture("entity/block/door/ladder_sealed.png"), 0),
		WARP_DOOR(new Texture("entity/block/door/challenge_door.png"), 0),

		LAMP(new Texture("entity/block/lamp.png"), 0, Color.YELLOW, Break.ICE),
		TORCH_ORANGE_LEFT(new Texture("entity/block/torch_left.png"), 0) {
			public void effect(Block block, CombatGear item) {
				if (item.stats().getAttribute(Special.ICE) != null) {
					block.setTypeAndSprite(Blocks.TORCH_BLUE_LEFT);
					if (block.getLight() != null) {
						block.getLight().setColor(LightColor.TORCH_BLUE.color);
					}
				}
			}
		},
		TORCH_ORANGE_RIGHT(new Texture("entity/block/torch_right.png"), 0) {
			public void effect(Block block, CombatGear item) {
				if (item.stats().getAttribute(Special.ICE) != null) {
					block.setTypeAndSprite(Blocks.TORCH_BLUE_RIGHT);
					if (block.getLight() != null) {
						block.getLight().setColor(LightColor.TORCH_BLUE.color);
					}
				}
			}
		},
		TORCH_BLUE_LEFT(new Texture("entity/block/torch_left_blue.png"), 0) {
			public void effect(Block block, CombatGear item) {
				if (item.stats().getAttribute(Special.FIRE) != null) {
					block.setTypeAndSprite(Blocks.TORCH_ORANGE_LEFT);
					if (block.getLight() != null) {
						block.getLight().setColor(LightColor.TORCH_ORANGE.color);
					}
				}
			}
		},
		TORCH_BLUE_RIGHT(new Texture("entity/block/torch_right_blue.png"), 0) {
			public void effect(Block block, CombatGear item) {
				if (item.stats().getAttribute(Special.FIRE) != null) {
					block.setTypeAndSprite(Blocks.TORCH_ORANGE_RIGHT);
					if (block.getLight() != null) {
						block.getLight().setColor(LightColor.TORCH_ORANGE.color);
					}
				}
			}
		},
		TORCH_RED_LEFT(new Texture("entity/block/torch_left_red.png"), 0),
		TORCH_RED_RIGHT(new Texture("entity/block/torch_right_red.png"), 0),
		
		MIRROR(new Texture("entity/block/modblock/mirror.png"), 0),
		SPEED(new Texture("entity/block/modblock/speed.png"), 0),
		SIZE(new Texture("entity/block/modblock/size.png"), 0),
		ONE_WAY(new Texture("entity/block/modblock/one_way.png"), 0),
		MOVE(new Texture("entity/block/modblock/move.png"), 0),
		GLASS(new Texture("entity/block/material/glass.png"), 0),
		BLANK(new Texture("screen/blank.png"), 0),

		TARGET(new Texture("entity/block/modblock/move.png"), 2),
		WEIGHT_PLATE(new Texture("entity/block/weighted_plate.png"), 0),
		
		LAVA(new Texture("entity/block/floor/lava.png"), 0),
		LAVA_C(new Texture("entity/block/floor/lava_c.png"), 0),
		MAGMA(new Texture("entity/block/floor/magma.png"), 0, Color.RED, Break.ICE),
		DIRT(new Texture("entity/block/floor/dirt.png"), 0),
		DIRT_C(new Texture("entity/block/floor/dirt_c.png"), 0),
		SAND(new Texture("entity/block/floor/sand.png"), 0),
		ICE(new Texture("entity/block/material/ice.png"), 0),
		NEST(new Texture("entity/block/nest.png"), 0),
		
		;
		
		public final Texture texture;
		public final double hardness;
		public final Color color;
		public final Animation[] animations;
		public final Break[] breaks;
		
		private Blocks(Texture texture, double hardness, Color color, Animation[] animations, Break[] breaks) {
			this.texture = texture;
			this.hardness = hardness;
			this.color = color;
			this.animations = animations;
			this.breaks = breaks;
		}

		
		private Blocks(Texture texture, double hardness, Animation... animations) {
			this(texture, hardness, Color.WHITE, animations, new Break[0]);
		}
		
		private Blocks(Texture texture, double hardness, Color color, Break... breaks) {
			this(texture, hardness, color, new Animation[0], breaks);
		}
		
		private Blocks(Texture texture, double hardness) {
			this(texture, hardness, Color.WHITE, new Animation[0], new Break[0]);
		}
		
		public boolean breaksBy(Break b) {
			if (breaks != null) {
				for (Break br : breaks) {
					if (br == b) {
						return true;
					}
				}
			}
			return false;
		}
		
		public void effect(Block block, CombatGear item) { }
		
		public enum Subtype {
			WOOD(Blocks.WOOD, Blocks.WOOD_LIGHT, Blocks.WOOD_DARK, Blocks.WOOD_RED, Blocks.WOOD_LIGHT, Blocks.WOOD_DARK),
			CHEST(Blocks.CHEST_WOOD, Blocks.CHEST_WOOD_LIGHT, Blocks.CHEST_WOOD_DARK, Blocks.CHEST_WOOD_RED, Blocks.CHEST_WOOD_BLACK, Blocks.CHEST_WOOD_WHITE),
			CRATE(Blocks.CRATE, Blocks.CRATE_LIGHT, Blocks.CRATE_DARK, Blocks.CRATE_RED, Blocks.CRATE_WHITE, Blocks.CRATE_BLACK),
			PACKED_COBBLE(Blocks.PACKED_COBBLE_BLACK, Blocks.PACKED_COBBLE_DARK_RED, Blocks.PACKED_COBBLE_RED),
			METAL(Blocks.METAL, Blocks.METAL_2, Blocks.METAL_3, Blocks.METAL_4, Blocks.METAL_5),
			CAVEROCK(Blocks.CAVEROCK, Blocks.CAVEROCK_BLACK, Blocks.CAVEROCK_RED),
			;
			
			public final List<Blocks> variants;
			
			private Subtype(Blocks... variants) {
				this.variants = Arrays.asList(variants);
			}
			
			public Blocks random() {
				return Realms.random(variants);
			}
		}
		
		public enum Animation {
			CHEST_WOOD_OPEN(new TextureRegion(new Texture("entity/block/chest/chest_wood_opening.png"), 0, 0, 70, 70)),
			CHEST_WOOD_DARK_OPEN(new TextureRegion(new Texture("entity/block/chest/chest_wood_dark_opening.png"), 0, 0, 70, 70)),
			CHEST_WOOD_LIGHT_OPEN(new TextureRegion(new Texture("entity/block/chest/chest_wood_light_opening.png"), 0, 0, 70, 70)),
			CHEST_WOOD_RED_OPEN(new TextureRegion(new Texture("entity/block/chest/chest_wood_red_opening.png"), 0, 0, 70, 70)),
			CHEST_WOOD_BLACK_OPEN(new TextureRegion(new Texture("entity/block/chest/chest_wood_black_opening.png"), 0, 0, 70, 70)),
			CHEST_WOOD_WHITE_OPEN(new TextureRegion(new Texture("entity/block/chest/chest_wood_white_opening.png"), 0, 0, 70, 70)),
			CHEST_MAGIC_OPEN(new TextureRegion(new Texture("entity/block/chest/chest_magic_opening.png"), 0, 0, 70, 70)),

			CHEST_EYES_OPEN(new TextureRegion(new Texture("entity/block/chest/chest_eyes_opening.png"), 0, 0, 70, 70)),
			CHEST_EYES_CLOSE(new TextureRegion(new Texture("entity/block/chest/chest_eyes_closing.png"), 0, 0, 70, 70)),
			;
			
			public final TextureRegion region;
			
			private Animation(TextureRegion region) {
				this.region = region;
			}
			
			public TextureRegion region() {
				return new TextureRegion(region.getTexture(), 0, 0, region.getRegionWidth(), region.getRegionHeight());
			}
		}
		
		public enum Break {
			AXE("sealed") {
				public boolean meetsItemCriteria(CombatGear item) {
					return item instanceof Sword && ((Sword)item).getAxe();
				}
			},
			EXPLODE("bolted") {
				private static final int SHRAPNEL_SPEED = 5;
				private final Crossbow shrapnel = (Crossbow)new Crossbow("", null, Model.NONE, Bolt.CIRCLE) {
					public void create(Projectile proj) {
						proj.setDecelerates(true);
						proj.setGroup(Group.NEUTRAL);
						proj.getDrop().stats().setStat(StatType.RANGE, StatType.RANGE.max, false);
						proj.getDrop().stats().setStat(StatType.AREA, 0, false);
					}
				}.setSpreadshot(4, 35, 0).resize(20, 20);
				
				public boolean meetsItemCriteria(CombatGear item) {
					return false;
				}
				
				public void effect(Block block, Projectile affecter) {
					if (affecter != null) {
						Vector2 direction = Realms.angleVector(block, affecter);
						Realms.getGame().getRoom().addEffects(Particle.create(block.getType(), block.centerX(), block.centerY(), (int)(SHRAPNEL_SPEED * direction.x), (int)(SHRAPNEL_SPEED * direction.y), 3, 10));
						shrapnel.setOwner(affecter.getUser());
						shrapnel.setStatistics(((Projectile)affecter).getDrop().stats().clone().encrypt("shrapnel"));
						shrapnel.setTint(block.getType().color);
						shrapnel.use(direction.x, direction.y, block, Group.NEUTRAL/*((Projectile)affecter).getGroup()*/);
						shrapnel.refreshCooldown();
					}
				}
			},
			FIRE("frozen") {
				public void effect(Block block, Projectile affecter) {
					Audio.play(Sounds.FIRE, block.centerX());
					Realms.getGame().getRoom().addEffects(Particle.create(Particle.Type.FIRE, block.centerX(), block.centerY(), 3, 3, 10, 80));
				}
				
				public boolean meetsItemCriteria(CombatGear item) { 
					return item.stats().getAttribute(Special.FIRE) != null; 
				}
			},
			ICE("charred shut") {
				public void effect(Block block, Projectile affecter) {
					Audio.play(Sounds.ICE_BREAK, block.centerX());
					Realms.getGame().getRoom().addEffects(Particle.create(Particle.Type.ICE_BREAK, block.centerX(), block.centerY(), 3, 3, 10, 80));
				}
				
				public boolean meetsItemCriteria(CombatGear item) { 
					return item.stats().getAttribute(Special.ICE) != null; 
				}
			},
			MAGIC("magically sealed"),
			;
			
			public String chestMessage;
			
			private Break(String chestMessage) {
				this.chestMessage = chestMessage;
			}
			
			public void effect(Block block, Projectile affecter) { }
			
			public boolean meetsItemCriteria(CombatGear item) { return false; }
		}
		
	}
			
	
	public void render(boolean frozen) {
		if (!frozen) {
			super.render();
			if (move != null) {
				move.render();
				if (move.done()) {
					move = null;
				}
			}
		}
	}
	
	public void drawBounds() {
		if (Realms.getGame().getBounds()) {
			Graphics.shapeRenderer.begin(ShapeType.Line);
			Graphics.rect(getX(), getY(), getWidth(), getHeight());
			Graphics.shapeRenderer.end();
		}
	}
	
	public void draw(boolean frozen) {
		if (!(this instanceof Polygon) || ((Polygon)this).isSquare()) {
			Graphics.begin();
			Graphics.setColor(tint);
			draw(getSprite(), rotation);
			for (TextureRegion crack : cracks) {
				draw(crack, rotation);
			}
			Graphics.resetColor();
			Graphics.end();
		}
		if (!frozen && animation != null) {
			int width = getSprite().getRegionWidth();
			if (getSprite().getRegionX() + width == getSprite().getTexture().getWidth()) {
				animation = null;
			} else {
				getSprite().setRegionX(getSprite().getRegionX() + width);
				getSprite().setRegionWidth(width);
			}
		}
		render(frozen);
		drawBounds();
	}
	
	public void initializeSubtype() {
		if (subtype != null) {
			type = subtype.random();
			setSprite(Realms.toRegion(type.texture));
		}
	}
	
	public void destroy() {
		setShouldDelete(true);
		if (this instanceof Chest) {
			((Chest)this).drop(null);
		}
		Realms.getGame().getRoom().addEffects(Particle.create(type, centerX(), centerY(), 2, 2, (int)Math.sqrt(area()), (int)Math.sqrt(area())/10));
		Audio.play(Sounds.BREAK, 1f, Realms.vary(1f, 0.25f), centerX());
	}
	
	public Block crack() {
		TextureRegion crack;
		do {
			crack = cracksDefault.get((int)(Math.random()*cracksDefault.size()));
		} while(cracks.contains(crack));
		cracks.add(crack);
		return this;
	}
	
	public boolean equals(Object e) {
		return super.equals(e) && e instanceof Block && type == ((Block)e).type && solid == ((Block)e).solid && rotation == ((Block)e).rotation;
	}
	
	
	public Block clone() {
		Block block = null;
		block = new Block(getX(), getY(), getWidth(), getHeight(), getCircular(), this.type);
		block.solid = solid;
		block.setSprite(getSprite());
		block.subtype = subtype;
		block.cracks = cracks;
		return block;
	}
	
	public static boolean openable(Keys key, boolean remove, int x) {
		return key == null || Realms.getGame().gui().inventory().findKey(key, remove, x) || Realms.getGame().gui().inventory().findKey(Keys.CURSED, remove, x);
	}
	
	public boolean isFillableChest() {
		return this instanceof Chest && Blocks.Subtype.CHEST.variants.contains(type);
	}
	
	public Block setShadows(float shadowA) {
		shadows = new Entity[3][3];
		if (getWidth() == 70 && getHeight() == 70) {
			shadows[1][1] = new Entity(getX() - 33, getY() - 33, shadow70x);
		} else if (getWidth() == 80 && getHeight() == 80) {
			shadows[1][1] = new Entity(getX() - 33, getY() - 33, shadow80x);
		} else {
			/* [00][01][02]
			 * [10][11][12]
			 * [20][21][22]
			 */
			shadows[0][0] = new Entity(getX() - 33, getY2(), new TextureRegion(shadow, 0, 0, 33, 33));
			shadows[0][1] = new Entity(getX(), getY2(), new TextureRegion(shadow, 0, 2913, getWidth(), 33));
			shadows[0][2] = new Entity(getX2(), getY2(), new TextureRegion(shadow, 33, 0, 33, 33));
			shadows[1][0] = new Entity(getX() - 33, getY(), new TextureRegion(shadow, 2913, 0, 33, getHeight()));
			shadows[1][2] = new Entity(getX2(), getY(), new TextureRegion(shadow, 2880, 0, 33, getHeight()));
			shadows[2][0] = new Entity(getX() - 33, getY() - 33, new TextureRegion(shadow, 0, 33, 33, 33));
			shadows[2][1] = new Entity(getX(), getY() - 33, new TextureRegion(shadow, 0, 2880, getWidth(), 33));
			shadows[2][2] = new Entity(getX2(), getY() - 33, new TextureRegion(shadow, 33, 33, 33, 33));
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					if (shadows[i][j] != null && (shadows[i][j].getX() < 0 || shadows[i][j].getX() == Realms.WIDTH || shadows[i][j].getY() < 0 || shadows[i][j].getY() == Realms.HEIGHT)) {
						shadows[i][j] = null;
					}
				}
			}
		}
		shadowed = true;
		this.shadowA = shadowA;
		return this;
	}
	
	public void resetShadows() {
		if (shadows != null) {
			setShadows(shadowA);
		}
	}
	
	public void clearShadows() {
		shadows = null;
		shadowed = false;
	}
	
	public void changePos(float x, float y) {
		super.changeX(x);
		super.changeY(y);
		if (shadows != null) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					if (shadows[i][j] != null) {
						shadows[i][j].changeX(x);
						shadows[i][j].changeY(y);
					}
				}
			}
		}
	}
	
	public void setShadowed(boolean shadowed) {
		this.shadowed = shadowed;
	}
	
	public boolean hasShadows() {
		return shadowed;
	}
	
	public void renderShadows() {
		Graphics.setA(tint.a*shadowA);
		if (shadows != null) {
			Graphics.begin();
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					if (shadows[i][j] != null) {
						if (i == 1 && j == 1) {
							Graphics.draw(shadows[i][j].getSprite(), shadows[i][j].getX(), shadows[i][j].getY(), shadows[i][j].getSprite().getRegionWidth()/2, shadows[i][j].getSprite().getRegionHeight()/2, shadows[i][j].getSprite().getRegionWidth(), shadows[i][j].getSprite().getRegionWidth(), getWidth()/((float)shadows[i][j].getSprite().getRegionWidth()-66), getHeight()/((float)shadows[i][j].getSprite().getRegionHeight()-66), 0);
						} else {
							Graphics.draw(shadows[i][j].getSprite(), shadows[i][j].getX(), shadows[i][j].getY());
						}
					}
				}
			}
			Graphics.end();
		}
		Graphics.resetColor();
	}
	
	public boolean getSolid() {
		return solid;
	}
	
	public Block setSolid(boolean solid) {
		this.solid = solid;
		return this;
	}
	
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}
	
	public int getRotation() {
		return rotation;
	}
	
	public MoveCommand getMove() {
		return move;
	}
	
	public Block setMove(MoveCommand move) {
		this.move = move;
		return this;
	}
	
	public Blocks getType() {
		return type;
	}
	
	public void setType(Blocks block) {
		this.type = block;
	}
	
	public void setTypeAndSprite(Blocks block) {
		this.type = block;
		setSprite(Realms.toRegion(block.texture));
	}
	
	public Subtype getSubtype() {
		return subtype;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Block> T setSubtype(Subtype subtype) {
		this.subtype = subtype;
		return (T)this;
	}
	
	public void setAnimation(Animation animation) {
		this.animation = animation;
		setSprite(animation.region());
	}
	
	
	public void setAnimation(int index) {
		setAnimation(type.animations[index]);
	}
	
	public void endAnimation(int index) {
		setSprite(type.animations[index].region());
		getSprite().setRegion(getSprite().getTexture().getWidth() - getSprite().getRegionWidth(), 0, getSprite().getRegionWidth(), getSprite().getRegionHeight());
	}
	
	public void setA(float a) {
		tint.a = a;
	}
	
	public float getA() {
		return tint.a;
	}
	
	public Color getColor() {
		return tint;
	}
	
	public void setColor(Color tint) {
		this.tint = tint;
	}
	
	public void setColor(float r, float g, float b, float a) {
		tint.set(r, g, b, a);
	}
	
	public void setColor(float r, float g, float b) {
		tint.set(r, g, b, tint.a);
	}
	
	public void changeColor(float r, float g, float b, float a) {
		tint.add(r, g, b, a);
	}
	
	public static class TextureWrapper {
		
		public enum Style {
			STRETCH,
			FILL,
			FIT
		}
		
		public static void set(ArrayList<Block> blocks, Texture texture, Style style, int xMax, int yMax) {
			int[] bounds = Realms.boundsOf(blocks, xMax, yMax);
			ArrayList<TextureRegion> regions = new ArrayList<TextureRegion>();
			for (Block block : blocks) {
				regions.add(new TextureRegion(texture, block.getX(), block.getY(), block.getWidth(), block.getHeight()));
			}
			for (TextureRegion region : regions) {
				region.setRegion(region.getRegionX() - bounds[0], bounds[3]-bounds[1] - region.getRegionHeight() - (region.getRegionY() - bounds[1]), region.getRegionWidth(), region.getRegionHeight());
			}
			bounds[2] -= bounds[0];
			bounds[3] -= bounds[1];
			bounds[0] = 0;
			bounds[1] = 0;
			double ratioX = (double)texture.getWidth()/bounds[2];
			double ratioY = (double)texture.getHeight()/bounds[3];
			switch (style) {
				case STRETCH:
					break;
				case FILL:
					ratioX = 1 - Math.max(Math.abs(1 - ratioX), Math.abs(1 - ratioY));
					ratioY = ratioX;
					break;
				case FIT:
					ratioX = 1 - Math.min(Math.abs(1 - ratioX), Math.abs(1 - ratioY));
					ratioY = ratioX;
					break;
				default:
					break;
			}
			for (TextureRegion region : regions) {
				region.setRegion((int)(region.getRegionX() * ratioX + 0.5), (int)(region.getRegionY() * ratioY + 0.5), (int)(region.getRegionWidth() * ratioX + 0.5), (int)(region.getRegionHeight() * ratioY + 0.5));
			}
			for (int i = 0; i < blocks.size(); i++) {
				blocks.get(i).setSprite(regions.get(i));
			}
		}
		
		public static ArrayList<Block> wrap(ArrayList<Block> blocks, Style style, Blocks type, int xMax, int yMax) {
			TextureWrapper.set(blocks, type.texture, style, xMax, yMax);
			for (Block block : blocks) {
				block.setType(type);
			}
			return blocks;
		}
		
		public static Block[] wrap(Block[] blocks, Style style, Blocks type) {
			return wrap(new ArrayList<Block>(Arrays.asList(blocks)), style, type, Realms.WIDTH, Realms.HEIGHT).toArray(new Block[blocks.length]);
		}
		
		public static ArrayList<Block> wrap(ArrayList<Block> blocks, Style style, Subtype type, int xMax, int yMax) {
			ArrayList<Block> blocksNew = wrap(blocks, style, type.random(), xMax, yMax);
//			for (Block block : blocksNew) {
//				block.setSubtype(type);
//			}
			return blocksNew;
		}
		
		//width/height = width/height of blocks (that can be further subdivided)
		public static ArrayList<Block> tile(boolean[][] grid, int startX, int startY, int width, int height, int sectionsX, int sectionsY, double cracked) {
			ArrayList<Block> blocks = new ArrayList<Block>();
			int x;
			int y = startY + (grid.length - 1)*height;
			for (boolean[] a : grid) {
				x = startX;
				for (boolean b : a) {
					if (b) {
						for (int i = 0; i < sectionsX; i++) {
							for (int j = 0; j < sectionsY; j++) {
								Block block = new Block(x + i*width/sectionsX, y + j*height/sectionsY, width/sectionsX, height/sectionsY, false);
								if (Math.random() < cracked) {
									block.crack();
								}
								blocks.add(block);
							}
						}
					}
					x += width;
				}
				y -= height;
			}
			return blocks;
		}
		
		//width/height = # of blocks
		public static boolean[][] fill(int width, int height, Function function, boolean fillUpper, boolean fillSides) {
			boolean[][] grid = new boolean[width][height];
			for (double x = 0; x < grid[0].length; x += .1) {
				double y = function.calculate(x);
				if (y >= 0 && y <= grid.length - 1) {
					try {
						grid[grid.length - 1 - (int)(y)][(int)Realms.round(x, 2)] = true;
					} catch (ArrayIndexOutOfBoundsException e) { }
				}
			}
			for (int a = 0; a < grid[0].length; a++) {
				boolean fill = false;
				if (fillUpper) {
					for (int b = grid.length - 1; b >= 0; b--) {
						if (fill) {
							grid[b][a] = true;
						}
						if (grid[b][a]) {
							fill = true;
						}
					}
				} else {
					for (int b = 0; b < grid.length; b++) {
						if (fill) {
							grid[b][a] = true;
						}
						if (grid[b][a]) {
							fill = true;
						}
					}
				}
			}
			return grid;
		}

		public static boolean[][] combineFills(ArrayList<boolean[][]> fills) {
			int width = 0;
			int height = 0;
			for (boolean[][] fill : fills) {
				if (width < fill[0].length) {
					width = fill[0].length;
				}
				if (height < fill.length) {
					height = fill.length;
				}
			}
			boolean[][] grid = new boolean[width][height];
			for (int a = 0; a < grid.length; a++) {
				for (int b = 0; b < grid[0].length; b++) {
					int count = 0;
					for (boolean[][] fill : fills) {
						if (a <= fill.length - 1 && b <= fill[0].length - 1 && fill[a][b]) {
							count++;
						}
					}
					if (count == fills.size()) {
						grid[a][b] = true;
					}
				}
			}
			return grid;
		}
		
		public static boolean[][] loadFile(String file) {
			try {
				String[] lines = Gdx.files.internal("texts/modules/" + file).readString().split("\n");
				boolean[][] grid = new boolean[Integer.parseInt(lines[0])][Integer.parseInt(lines[1])];
				for (int i = 2; i < lines.length; i++) {
					for (int j = 0; j < lines[i].length(); j++) {
						if (lines[i].charAt(j) == '1') {
							grid[i - 2][j] = true;
						}
					}
				}
				return grid;
			} catch (NumberFormatException e) {
				return new boolean[0][0];
			}
		}
		
		public static boolean[][] rectangle(int widthMax, int heightMax, int widthBorder, int heightBorder) {
			boolean[][] grid = new boolean[heightMax][widthMax];
			for (int x = 0; x < widthMax; x++) {
				for (int y = 0; y < heightMax; y++) {
					grid[y][x] = true;
				}
			}
			for (int x = widthBorder; x < widthMax - widthBorder; x++) {
				for (int y = heightBorder; y < heightMax - heightBorder; y++) {
					grid[y][x] = false;
				}
			}
			/*
			for (int b = 0; b < widthMax; b++) {
				for (int a = heightMax - widthBorder; a < heightMax; a++) {
					grid[b][a] = true;
				}
				for (int a = 0; a < widthBorder; a++) {
					grid[b][a] = true;
				}
			}
			for (int a = 0; a < heightMax; a++) {
				for (int b = widthMax - heightBorder; b < widthMax; b++) {
					grid[b][a] = true;
				}
				for (int b = 0; b < heightBorder; b++) {
					grid[b][a] = true;
				}
			}
			*/
			return grid;
		}

		public static boolean[][] ellipse(int aMax, int bMax, int aMin, int bMin, int rMin, int rMax) {
			boolean[][] grid = new boolean[aMax*2][bMax*2];
			for (int a = aMax; a >= aMin; a--) {
				for (int b = bMax; b >= bMin; b--) {
					for (int r = rMin; r < rMax; r++) {
						grid[aMax + (int)(a*Math.cos(Math.toRadians(r)) - .5)][bMax + (int)(b*Math.sin(Math.toRadians(r)) - .5)] = true;
					}
				}
			}
			return grid;
		}
		
		public static boolean[][] ellipse(int aMax, int bMax, int aMin, int bMin) {
			return ellipse(aMax, bMax, aMin, bMin, 0, 360);
		}
		
		
		public static Block[] combine(Block[]... blocks2d) {
			int count = 0;
			for (Block[] blocks : blocks2d) {
				count += blocks.length;
			}
			Block[] blocksTotal = new Block[count];
			int i = 0;
			for (Block[] blocks : blocks2d) {
				for (Block block : blocks) {
					blocksTotal[i] = block;
					i++;
				}
			}
			return blocksTotal;
		}
		
		public static Block[] grid(int x, int y, int width, int height, Blocks type, int xCount, int yCount) {
			Block[] blocks = new Block[xCount*yCount];
			int i = 0;
			for (int a = 0; a < xCount; a++) {
				for (int b = 0; b < yCount; b++) {
					blocks[i] = new Block(x + width*a, y + height*b, width, height, type);
					i++;
				}
			}
			return blocks;
		}
		
		public static boolean[][] toBoolGrid(int[][] grid) {
			boolean[][] bool = new boolean[grid.length][grid[0].length];
			for (int r = 0; r < grid.length; r++) {
				for (int c = 0; c < grid[0].length; c++) {
					bool[r][c] = grid[r][c] == 1 ? true : false;
				}
			}
			return bool;
		}
		
	}
	
}
