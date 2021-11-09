package net.obmc.OBChestShop.Utils;

import com.google.gson.*;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.logging.Logger;

/**
 * Parse {@link ItemStack} to JSON
 *
 * @author DevSrSouza
 * @version 1.0
 *
 * https://github.com/DevSrSouza/
 * You can find updates here https://gist.github.com/DevSrSouza
 */

public class JsonItemStack {

	static Logger log = Logger.getLogger("Minecraft");
	
    /**
     * Parse the {@link ItemStack} to JSON
     *
     * @param itemStack The {@link ItemStack} instance
     * @return The JSON string
     */
    public static String toJson(ItemStack itemStack) {

        Gson gson = new Gson();
        JsonObject itemJson = new JsonObject();

        itemJson.addProperty("type", itemStack.getType().name());
        //if (itemStack.getDurability() > 0) itemJson.addProperty("data", itemStack.getDurability());
        //if (itemStack.getAmount() != 1) itemJson.addProperty("amount", itemStack.getAmount());


        if (itemStack.hasItemMeta()) {

        	JsonObject metaJson = new JsonObject();
            ItemMeta meta = itemStack.getItemMeta();

            //
            // save itemstack base data common to all itemstacks
            //

            // display name
            if (meta.hasDisplayName()) {
                metaJson.addProperty("displayname", meta.getDisplayName());
            }
            // lore
            if (meta.hasLore()) {
                JsonArray lore = new JsonArray();
                meta.getLore().forEach(str -> lore.add(new JsonPrimitive(str)));
                metaJson.add("lore", lore);
            }
            // enchantments - add to collection, sort and push into json array
            if (meta.hasEnchants()) {
                JsonArray enchantsJson = new JsonArray();
                List<String> enchants = new ArrayList<String>();
                meta.getEnchants().forEach((enchantment, integer) -> {
                	enchants.add(enchantment.getKey().toString() + ":" + integer);
                });
                Collections.sort(enchants);
                enchants.forEach((enchantment) -> {
                	enchantsJson.add(new JsonPrimitive(enchantment));
                });
                metaJson.add("enchants", enchantsJson);
            }
            // flags
            if (!meta.getItemFlags().isEmpty()) {
                JsonArray flags = new JsonArray();
                meta.getItemFlags().stream().map(ItemFlag::name).forEach(str -> flags.add(new JsonPrimitive(str)));
                metaJson.add("flags", flags);
            }

            //
            // save itemstack meta unique to each itemstack type
            //

            JsonObject extraMeta = new JsonObject();
            
            // skull - only works for players who have joined the server
            if (meta instanceof SkullMeta) {
    			SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
    			if (skullMeta.hasOwner()) {
    				String playerid = skullMeta.getOwningPlayer().getUniqueId().toString();
    				extraMeta.addProperty("owner", playerid);
    				metaJson.add("extra-meta", extraMeta);
    			}

            // banner
            } else if (meta instanceof BannerMeta) {
            	BannerMeta bannerMeta = (BannerMeta) meta;
                if (bannerMeta.numberOfPatterns() > 0) {
                    JsonArray patterns = new JsonArray();
                    bannerMeta.getPatterns()
                            .stream()
                            .map(pattern ->
                                    pattern.getColor().name() + ":" + pattern.getPattern().getIdentifier())
                            .forEach(str -> patterns.add(new JsonPrimitive(str)));
                    extraMeta.add("patterns", patterns);
                }
                metaJson.add("extra-meta", extraMeta);

            // stored enchantments eg. enchantment books
            } else if (meta instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta esmeta = (EnchantmentStorageMeta) meta;
                if (esmeta.hasStoredEnchants()) {
                    JsonArray storedEnchants = new JsonArray();
                    esmeta.getStoredEnchants().forEach((enchantment, level) -> {
                        storedEnchants.add(new JsonPrimitive(enchantment.getKey().toString() + ":" + level));
                    });
                    extraMeta.add("stored-enchants", storedEnchants);
                    metaJson.add("extra-meta", extraMeta);
                }

            // leather armor
            } else if (meta instanceof LeatherArmorMeta) {
                LeatherArmorMeta lameta = (LeatherArmorMeta) meta;
                extraMeta.addProperty("color", Integer.toHexString(lameta.getColor().asRGB()));
                metaJson.add("extra-meta", extraMeta);
            // book and quill
            } else if (meta instanceof BookMeta) {
            	BookMeta bookMeta = (BookMeta) meta;
                if (bookMeta.hasAuthor() || bookMeta.hasPages() || bookMeta.hasTitle()) {
                    if (bookMeta.hasTitle()) {
                        extraMeta.addProperty("title", bookMeta.getTitle());
                    }
                    if (bookMeta.hasAuthor()) {
                        extraMeta.addProperty("author", bookMeta.getAuthor());
                    }
                    if (bookMeta.hasPages()) {
                        JsonArray pages = new JsonArray();
                        bookMeta.getPages().forEach(str -> pages.add(new JsonPrimitive(str)));
                        extraMeta.add("pages", pages);
                    }
                    metaJson.add("extra-meta", extraMeta);
                }

            // potions
            } else if (meta instanceof PotionMeta) {
                PotionMeta pmeta = (PotionMeta) meta;
                if (pmeta.hasCustomEffects()) {
                    JsonArray customEffects = new JsonArray();
                    pmeta.getCustomEffects().forEach(potionEffect -> {
                        customEffects.add(new JsonPrimitive(potionEffect.getType().getName()
                                + ":" + potionEffect.getAmplifier()
                                + ":" + potionEffect.getDuration() / 20));
                    });
                    extraMeta.add("custom-effects", customEffects);
                    metaJson.add("extra-meta", extraMeta);
                } else {
                	PotionData pdata = pmeta.getBasePotionData();
                	JsonArray baseEffects = new JsonArray();
                	baseEffects.add(new JsonPrimitive(pdata.getType().name()
                			+ ":" + pdata.isExtended()
                			+ ":" + pdata.isUpgraded()));
                	extraMeta.add("base-effects", baseEffects);
                	metaJson.add("extra-meta", extraMeta);	
                }
                if (pmeta.hasColor()) {
                	metaJson.addProperty("color", pmeta.getColor().asRGB());
                }

            // fireworks
            } else if (meta instanceof FireworkEffectMeta) {
                FireworkEffectMeta femeta = (FireworkEffectMeta) meta;
                if (femeta.hasEffect()) {
                    FireworkEffect effect = femeta.getEffect();

                    extraMeta.addProperty("type", effect.getType().name());
                    if (effect.hasFlicker()) extraMeta.addProperty("flicker", true);
                    if (effect.hasTrail()) extraMeta.addProperty("trail", true);

                    if (!effect.getColors().isEmpty()) {
                        JsonArray colors = new JsonArray();
                        effect.getColors().forEach(color ->
                                colors.add(new JsonPrimitive(Integer.toHexString(color.asRGB()))));
                        extraMeta.add("colors", colors);
                    }

                    if (!effect.getFadeColors().isEmpty()) {
                        JsonArray fadeColors = new JsonArray();
                        effect.getFadeColors().forEach(color ->
                                fadeColors.add(new JsonPrimitive(Integer.toHexString(color.asRGB()))));
                        extraMeta.add("fade-colors", fadeColors);
                    }
                    metaJson.add("extra-meta", extraMeta);
                }
            } else if (meta instanceof FireworkMeta) {
                FireworkMeta fmeta = (FireworkMeta) meta;

                extraMeta.addProperty("power", fmeta.getPower());

                if (fmeta.hasEffects()) {
                    JsonArray effects = new JsonArray();
                    fmeta.getEffects().forEach(effect -> {
                        JsonObject jsonObject = new JsonObject();

                        jsonObject.addProperty("type", effect.getType().name());
                        if (effect.hasFlicker()) jsonObject.addProperty("flicker", true);
                        if (effect.hasTrail()) jsonObject.addProperty("trail", true);

                        if (!effect.getColors().isEmpty()) {
                            JsonArray colors = new JsonArray();
                            effect.getColors().forEach(color ->
                                    colors.add(new JsonPrimitive(Integer.toHexString(color.asRGB()))));
                            jsonObject.add("colors", colors);
                        }

                        if (!effect.getFadeColors().isEmpty()) {
                            JsonArray fadeColors = new JsonArray();
                            effect.getFadeColors().forEach(color ->
                                    fadeColors.add(new JsonPrimitive(Integer.toHexString(color.asRGB()))));
                            jsonObject.add("fade-colors", fadeColors);
                        }

                        effects.add(jsonObject);
                    });
                    extraMeta.add("effects", effects);
                }
                metaJson.add("extra-meta", extraMeta);

            // maps
            } else if (meta instanceof MapMeta) {
                MapMeta mmeta = (MapMeta) meta;
                if (mmeta.hasLocationName()) {
                    extraMeta.addProperty("location-name", mmeta.getLocationName());
                }
                if (mmeta.hasColor()) {
                    extraMeta.addProperty("color", Integer.toHexString(mmeta.getColor().asRGB()));
                }
                if (mmeta.hasMapView()) {
                	MapView view = mmeta.getMapView();
                	JsonArray mapview = new JsonArray();
                	mapview.add(view.getCenterX());
                	mapview.add(view.getCenterZ());
                	mapview.add(view.getScale().toString());
                	mapview.add(view.getWorld().getName());
                	mapview.add(view.isLocked() ? "true" : "false");
                	mapview.add(view.isTrackingPosition() ? "true" : "false");
                	mapview.add(view.isUnlimitedTracking() ? "true" : "false");
                	mapview.add(view.isVirtual() ? "true" : "false");
                	extraMeta.add("mapview", mapview);
                }
                extraMeta.addProperty("scaling", mmeta.isScaling());
                metaJson.add("extra-meta", extraMeta);
            }
            itemJson.add("item-meta", metaJson);
        }
        return gson.toJson(itemJson);
    }

    /**
     * Parse a JSON to {@link ItemStack}
     *
     * @param string The JSON string
     * @return The {@link ItemStack} or null if not succeed
     */
    public static ItemStack fromJson(String itemstring) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(itemstring);
        if (element.isJsonObject()) {
            JsonObject itemJson = element.getAsJsonObject();

            JsonElement typeElement = itemJson.get("type");
            //JsonElement dataElement = itemJson.get("data");
            //JsonElement amountElement = itemJson.get("amount");

            if (typeElement.isJsonPrimitive()) {

            	//
                // apply itemstack base data common to all itemstacks
                //
            	
                String type = typeElement.getAsString();
                //short data = dataElement != null ? dataElement.getAsShort() : 0;
                //int amount = amountElement != null ? amountElement.getAsInt() : 1;

                ItemStack itemStack = new ItemStack(Material.getMaterial(type), 1);
                //itemStack.setDurability(data);
                //itemStack.setAmount(amount);

                JsonElement itemMetaElement = itemJson.get("item-meta");
                if (itemMetaElement != null && itemMetaElement.isJsonObject()) {
                    ItemMeta meta = itemStack.getItemMeta();
                    JsonObject metaJson = itemMetaElement.getAsJsonObject();

                    // display name
                    JsonElement displaynameElement = metaJson.get("displayname");
                    if (displaynameElement != null && displaynameElement.isJsonPrimitive()) {
                        meta.setDisplayName(displaynameElement.getAsString());
                    }
                    // lore
                    JsonElement loreElement = metaJson.get("lore");
                    if (loreElement != null && loreElement.isJsonArray()) {
                        JsonArray jarray = loreElement.getAsJsonArray();
                        List<String> lore = new ArrayList<>(jarray.size());
                        jarray.forEach(jsonElement -> {
                            if (jsonElement.isJsonPrimitive()) lore.add(jsonElement.getAsString());
                        });
                        meta.setLore(lore);
                    }
                    // enchantments
                    JsonElement enchants = metaJson.get("enchants");
                    if (enchants != null && enchants.isJsonArray()) {
                        JsonArray jarray = enchants.getAsJsonArray();
                        jarray.forEach(jsonElement -> {
                            if (jsonElement.isJsonPrimitive()) {
                                String enchantString = jsonElement.getAsString();
                                if (enchantString.contains(":")) {
                                    try {
                                        String[] splitEnchant = enchantString.split(":");
                                       	NamespacedKey key = NamespacedKey.fromString(splitEnchant[0] + ":" + splitEnchant[1]);
                                       	Enchantment enchantment = Enchantment.getByKey(key);
                                       	int level = Integer.parseInt(splitEnchant[2]);
                                       	if (enchantment != null && level > 0) {
                                      		meta.addEnchant(enchantment, level, true);
                                       	}
                                    } catch (NumberFormatException ex) {
                                    }
                                }
                            }
                        });
                    }
                    // flags
                    JsonElement flagsElement = metaJson.get("flags");
                    if (flagsElement != null && flagsElement.isJsonArray()) {
                        JsonArray jarray = flagsElement.getAsJsonArray();
                        jarray.forEach(jsonElement -> {
                            if (jsonElement.isJsonPrimitive()) {
                                for (ItemFlag flag : ItemFlag.values()) {
                                    if (flag.name().equalsIgnoreCase(jsonElement.getAsString())) {
                                        meta.addItemFlags(flag);
                                        break;
                                    }
                                }
                            }
                        });
                    }

                    //
                    // apply meta unique to each itemstack type
                    //
                    
                    JsonElement extrametaElement = metaJson.get("extra-meta");
                    if (extrametaElement != null && extrametaElement.isJsonObject()) {
                        try {
                            JsonObject extraJson = extrametaElement.getAsJsonObject();
                            
                            // skull
                            if (meta instanceof SkullMeta) {
                                JsonElement ownerElement = extraJson.get("owner");
                                if (ownerElement != null && ownerElement.isJsonPrimitive()) {
                                    SkullMeta skullMeta = (SkullMeta) meta;
                                    skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(ownerElement.getAsString())));
                                }

                            // banner
                            } else if (meta instanceof BannerMeta) {
                                JsonElement patternsElement = extraJson.get("patterns");
                                BannerMeta bmeta = (BannerMeta) meta;
                                if (patternsElement != null && patternsElement.isJsonArray()) {
                                    JsonArray jarray = patternsElement.getAsJsonArray();
                                    List<Pattern> patterns = new ArrayList<>(jarray.size());
                                    jarray.forEach(jsonElement -> {
                                        String patternString = jsonElement.getAsString();
                                        if (patternString.contains(":")) {
                                            String[] splitPattern = patternString.split(":");
                                            Optional<DyeColor> color = Arrays.stream(DyeColor.values())
                                                    .filter(dyeColor -> dyeColor.name().equalsIgnoreCase(splitPattern[0]))
                                                    .findFirst();
                                            PatternType patternType = PatternType.getByIdentifier(splitPattern[1]);
                                            if (color.isPresent() && patternType != null) {
                                                patterns.add(new Pattern(color.get(), patternType));
                                            }
                                        }
                                    });
                                    if (!patterns.isEmpty()) bmeta.setPatterns(patterns);
                                }

							// stored enchantments eg. enchantment books
                            } else if (meta instanceof EnchantmentStorageMeta) {
                                JsonElement storedEnchantsElement = extraJson.get("stored-enchants");
                                if (storedEnchantsElement != null && storedEnchantsElement.isJsonArray()) {
                                    EnchantmentStorageMeta esmeta = (EnchantmentStorageMeta) meta;
                                    JsonArray jarray = storedEnchantsElement.getAsJsonArray();
                                    jarray.forEach(jsonElement -> {
                                        if (jsonElement.isJsonPrimitive()) {
                                            String enchantString = jsonElement.getAsString();
                                            if (enchantString.contains(":")) {
                                                try {
                                                    String[] splitEnchant = enchantString.split(":");
                                                   	NamespacedKey key = NamespacedKey.fromString(splitEnchant[0] + ":" + splitEnchant[1]);
                                                    Enchantment enchantment = Enchantment.getByKey(key);
                                                    int level = Integer.parseInt(splitEnchant[2]);
                                                    if (enchantment != null && level > 0) {
                                                        esmeta.addStoredEnchant(enchantment, level, true);
                                                    }
                                                } catch (NumberFormatException ex) {
                                                }
                                            }
                                        }
                                    });
                                }

                            // leather armor
                            } else if (meta instanceof LeatherArmorMeta) {
                                JsonElement colorElement = extraJson.get("color");
                                if (colorElement != null && colorElement.isJsonPrimitive()) {
                                    LeatherArmorMeta lameta = (LeatherArmorMeta) meta;
                                    try {
                                        lameta.setColor(Color.fromRGB(Integer.parseInt(colorElement.getAsString(), 16)));
                                    } catch (NumberFormatException ex) {
                                    }
                                }

                            // book and quill
                            } else if (meta instanceof BookMeta) {
                                JsonElement titleElement = extraJson.get("title");
                                JsonElement authorElement = extraJson.get("author");
                                JsonElement pagesElement = extraJson.get("pages");

                                BookMeta bmeta = (BookMeta) meta;
                                if (titleElement != null && titleElement.isJsonPrimitive()) {
                                    bmeta.setTitle(titleElement.getAsString());
                                }
                                if (authorElement != null && authorElement.isJsonPrimitive()) {
                                    bmeta.setAuthor(authorElement.getAsString());
                                }
                                if (pagesElement != null && pagesElement.isJsonArray()) {
                                    JsonArray jarray = pagesElement.getAsJsonArray();
                                    List<String> pages = new ArrayList<>(jarray.size());
                                    jarray.forEach(jsonElement -> {
                                        if (jsonElement.isJsonPrimitive()) pages.add(jsonElement.getAsString());
                                    });
                                    bmeta.setPages(pages);
                                }

                            // potions
                            } else if (meta instanceof PotionMeta) {
                            	JsonElement customEffectsElement = extraJson.get("custom-effects");
                                if (customEffectsElement != null && customEffectsElement.isJsonArray()) {
                                    PotionMeta pmeta = (PotionMeta) meta;
                                    JsonArray jarray = customEffectsElement.getAsJsonArray();
                                    jarray.forEach(jsonElement -> {
                                        if (jsonElement.isJsonPrimitive()) {
                                            String enchantString = jsonElement.getAsString();
                                            if (enchantString.contains(":")) {
                                                try {
                                                    String[] splitPotions = enchantString.split(":");
                                                    PotionEffectType potionType = PotionEffectType.getByName(splitPotions[0]);
                                                    int amplifier = Integer.parseInt(splitPotions[1]);
                                                    int duration = Integer.parseInt(splitPotions[2]) * 20;
                                                    if (potionType != null) {
                                                        pmeta.addCustomEffect(new PotionEffect(potionType, amplifier, duration), true);
                                                    }
                                                } catch (NumberFormatException ex) {
                                                }
                                            }
                                        }
                                    });
                                }
                                JsonElement baseEffects = extraJson.get("base-effects");
                                if (baseEffects != null && baseEffects.isJsonArray()) {
                                	PotionMeta pmeta = (PotionMeta) meta;
                                	JsonArray jarray = baseEffects.getAsJsonArray();
                                	jarray.forEach(jsonElement -> {
                                		if (jsonElement.isJsonPrimitive()) {
                                			String effectString = jsonElement.getAsString();
                                			if (effectString.contains(":")) {
                                				String[] splitEffect = effectString.split(":");
                                				PotionData pdata = new PotionData(PotionType.valueOf(splitEffect[0]), Boolean.valueOf(splitEffect[1]), Boolean.valueOf(splitEffect[2]));
                                				pmeta.setBasePotionData(pdata);
                                			}
                                		}
                                	});
                                }

                            // fireworks
                            } else if (meta instanceof FireworkEffectMeta) {
                                JsonElement effectTypeElement = extraJson.get("type");
                                JsonElement flickerElement = extraJson.get("flicker");
                                JsonElement trailElement = extraJson.get("trail");
                                JsonElement colorsElement = extraJson.get("colors");
                                JsonElement fadeColorsElement = extraJson.get("fade-colors");

                                if (effectTypeElement != null && effectTypeElement.isJsonPrimitive()) {
                                    FireworkEffectMeta femeta = (FireworkEffectMeta) meta;

                                    FireworkEffect.Type effectType = FireworkEffect.Type.valueOf(effectTypeElement.getAsString());

                                    if (effectType != null) {
                                        List<Color> colors = new ArrayList<>();
                                        if (colorsElement != null && colorsElement.isJsonArray())
                                            colorsElement.getAsJsonArray().forEach(colorElement -> {
                                                if (colorElement.isJsonPrimitive())
                                                    colors.add(Color.fromRGB(Integer.parseInt(colorElement.getAsString(), 16)));
                                            });

                                        List<Color> fadeColors = new ArrayList<>();
                                        if (fadeColorsElement != null && fadeColorsElement.isJsonArray())
                                            fadeColorsElement.getAsJsonArray().forEach(colorElement -> {
                                                if (colorElement.isJsonPrimitive())
                                                    fadeColors.add(Color.fromRGB(Integer.parseInt(colorElement.getAsString(), 16)));
                                            });

                                        FireworkEffect.Builder builder = FireworkEffect.builder().with(effectType);

                                        if (flickerElement != null && flickerElement.isJsonPrimitive())
                                            builder.flicker(flickerElement.getAsBoolean());
                                        if (trailElement != null && trailElement.isJsonPrimitive())
                                            builder.trail(trailElement.getAsBoolean());

                                        if (!colors.isEmpty()) builder.withColor(colors);
                                        if (!fadeColors.isEmpty()) builder.withFade(fadeColors);

                                        femeta.setEffect(builder.build());
                                    }
                                }
                            } else if (meta instanceof FireworkMeta) {
                                FireworkMeta fmeta = (FireworkMeta) meta;

                                JsonElement effectArrayElement = extraJson.get("effects");
                                JsonElement powerElement = extraJson.get("power");

                                if (powerElement != null && powerElement.isJsonPrimitive()) {
                                    fmeta.setPower(powerElement.getAsInt());
                                }

                                if (effectArrayElement != null && effectArrayElement.isJsonArray()) {

                                    effectArrayElement.getAsJsonArray().forEach(jsonElement -> {
                                        if (jsonElement.isJsonObject()) {

                                            JsonObject jsonObject = jsonElement.getAsJsonObject();

                                            JsonElement effectTypeElement = jsonObject.get("type");
                                            JsonElement flickerElement = jsonObject.get("flicker");
                                            JsonElement trailElement = jsonObject.get("trail");
                                            JsonElement colorsElement = jsonObject.get("colors");
                                            JsonElement fadeColorsElement = jsonObject.get("fade-colors");

                                            if (effectTypeElement != null && effectTypeElement.isJsonPrimitive()) {

                                                FireworkEffect.Type effectType = FireworkEffect.Type.valueOf(effectTypeElement.getAsString());

                                                if (effectType != null) {
                                                    List<Color> colors = new ArrayList<>();
                                                    if (colorsElement != null && colorsElement.isJsonArray())
                                                        colorsElement.getAsJsonArray().forEach(colorElement -> {
                                                            if (colorElement.isJsonPrimitive())
                                                                colors.add(Color.fromRGB(Integer.parseInt(colorElement.getAsString(), 16)));
                                                        });

                                                    List<Color> fadeColors = new ArrayList<>();
                                                    if (fadeColorsElement != null && fadeColorsElement.isJsonArray())
                                                        fadeColorsElement.getAsJsonArray().forEach(colorElement -> {
                                                            if (colorElement.isJsonPrimitive())
                                                                fadeColors.add(Color.fromRGB(Integer.parseInt(colorElement.getAsString(), 16)));
                                                        });

                                                    FireworkEffect.Builder builder = FireworkEffect.builder().with(effectType);

                                                    if (flickerElement != null && flickerElement.isJsonPrimitive())
                                                        builder.flicker(flickerElement.getAsBoolean());
                                                    if (trailElement != null && trailElement.isJsonPrimitive())
                                                        builder.trail(trailElement.getAsBoolean());

                                                    if (!colors.isEmpty()) builder.withColor(colors);
                                                    if (!fadeColors.isEmpty()) builder.withFade(fadeColors);

                                                    fmeta.addEffect(builder.build());
                                                }
                                            }
                                        }
                                    });
                                }

                            // map
                            } else if (meta instanceof MapMeta) {
                                MapMeta mmeta = (MapMeta) meta;
                                JsonElement locationName = extraJson.get("location-name");
                                if (locationName != null && locationName.isJsonPrimitive()) {
                                	mmeta.setLocationName(locationName.getAsString());
                                }
                                JsonElement colorElement = extraJson.get("color");
                                if (colorElement != null && colorElement.isJsonPrimitive()) {
                                	mmeta.setColor(Color.fromRGB(Integer.parseInt(colorElement.getAsString(), 16)));
                                }
                                JsonElement scalingElement = extraJson.get("scaling");
                                if (scalingElement != null && scalingElement.isJsonPrimitive()) {
                                    mmeta.setScaling(scalingElement.getAsBoolean());
                                }
                                JsonElement mapViewElement = extraJson.get("mapview");
                                if (mapViewElement != null && mapViewElement.isJsonArray()) {
                                	JsonArray mapViewAttributes = mapViewElement.getAsJsonArray();
                                	MapView mapView = Bukkit.createMap(Bukkit.getWorld(mapViewAttributes.get(3).getAsString()));
                                	mapView.setCenterX(mapViewAttributes.get(0).getAsInt());
                                	mapView.setCenterZ(mapViewAttributes.get(1).getAsInt());
                                	mapView.setScale(Scale.valueOf(mapViewAttributes.get(2).getAsString()));
                                	mapView.setWorld(Bukkit.getWorld(mapViewAttributes.get(3).getAsString()));
                                	mapView.setLocked(mapViewAttributes.get(4).getAsBoolean());
                                	mapView.setTrackingPosition(mapViewAttributes.get(5).getAsBoolean());
                                	mapView.setUnlimitedTracking(mapViewAttributes.get(6).getAsBoolean());
                                	mmeta.setMapView(mapView);
                                }
                            }
                        } catch (Exception e) {
                        	e.printStackTrace();
                        	return null;
                        }
                    }
                    itemStack.setItemMeta(meta);
                }
                return itemStack;
            } else return null;
        } else return null;
    }
}