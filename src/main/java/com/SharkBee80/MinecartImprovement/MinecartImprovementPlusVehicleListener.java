package com.SharkBee80.MinecartImprovement;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Tag;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.material.Rails;
import org.bukkit.util.Vector;

public class MinecartImprovementPlusVehicleListener implements Listener {
    // 用于存储 Minecart 和它的cartMaxSpeeds值
    public static final HashMap<Minecart, Double> cartMaxSpeeds = new HashMap<>();

    // 用于存储 Minecart 和它的 slow-boolean 属性
    public static final HashMap<Minecart, Boolean> cartslow = new HashMap<>();

    public static boolean isSign(Material m) {
        return Tag.SIGNS.isTagged(m);
    }

    // 自定义方法，用于获取 Minecart 的 boolean 属性
    public boolean slow_flag(Minecart cart) {
        return cartslow.getOrDefault(cart, true); // 如果没有设置，返回默认值 true
    }

    private final static int BUFFER_LENGTH = 4;  //3,8\\//4,16\\//5,20\\
    private final static int ADJUST_LENGTH = 16;
    private final static double NORMAL_SPEED = 0.4;


    private final double setSpeed = 0.4D * MinecartImprovement.getSpeedMultiplier();

    // 告示牌偏移
    int[] xmodifier = {-1, 0, 1};
    int[] ymodifier = {-2, -1, 0, 1, 2};
    int[] zmodifier = {-1, 0, 1};

    int cartx, carty, cartz;
    int blockx, blocky, blockz;

    Block block;
    int blockid;

    double line1;

    public static MinecartImprovement plugin;
    Logger log = Logger.getLogger("Minecraft");

    boolean error;

    Vector flyingmod = new Vector(10, 0.01, 10);
    Vector noflyingmod = new Vector(1, 1, 1);

    public MinecartImprovementPlusVehicleListener(MinecartImprovement instance) {
        plugin = instance;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleCreate(VehicleCreateEvent event) {
        if (event.getVehicle() instanceof Minecart) {

            Minecart cart = (Minecart) event.getVehicle();

            cart.setMaxSpeed(setSpeed);
            cartMaxSpeeds.put(cart, setSpeed);
            cartslow.put(cart, true);

        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleMove(VehicleMoveEvent event) {

        if (event.getVehicle() instanceof Minecart) {

            Minecart cart = (Minecart) event.getVehicle();

            double normal_speed = NORMAL_SPEED;

            if (NORMAL_SPEED > cartMaxSpeeds.getOrDefault(cart, NORMAL_SPEED)) {
                normal_speed = cartMaxSpeeds.getOrDefault(cart, NORMAL_SPEED);
            }

            if (slow_flag(cart)) {
                Block curBlock = cart.getLocation().getBlock();
                if (!isRail(curBlock)) {
                    cart.setMaxSpeed(normal_speed);
                    return;
                }

                Rails curRail = (Rails) curBlock.getState().getData();
                RailType curRailType = RailType.get(curRail);
                if (curRailType != RailType.X_FLAT && curRailType != RailType.Z_FLAT) {
                    cart.setMaxSpeed(normal_speed);
                    return;
                }

                Vector vector = cart.getVelocity();
                if (vector.getY() != 0) {
                    cart.setMaxSpeed(normal_speed);
                    return;
                }
                double x = vector.getX();
                double z = vector.getZ();

                if (x == 0 && z == 0) {
                    cart.setMaxSpeed(normal_speed);
                    return;
                }

                boolean isX = x != 0 && z == 0;
                boolean n = isX ? x < 0 : z < 0;
                BlockFace direction = isX ? (n ? BlockFace.WEST : BlockFace.EAST) : (n ? BlockFace.NORTH : BlockFace.SOUTH);

                int flatLength = 0;
                while ((curBlock = nextRail(direction, curBlock)) != null && flatLength < BUFFER_LENGTH + ADJUST_LENGTH) {
                    RailType railType = RailType.get((Rails) curBlock.getState().getData());
                    if (isX) {
                        if (railType != RailType.X_FLAT && railType != RailType.X_SLOPE) break;
                    } else {
                        if (railType != RailType.Z_FLAT && railType != RailType.Z_SLOPE) break;
                    }

                    flatLength++;
                }
                //*
                if (flatLength < BUFFER_LENGTH) {
                    cart.setMaxSpeed(normal_speed);
                    return;
                }
                //*/

                int freeLength = flatLength - BUFFER_LENGTH;

                double s = (double) freeLength / ADJUST_LENGTH;
                if (s > 1) s = 1;
                double speed = normal_speed + (cartMaxSpeeds.getOrDefault(cart, normal_speed) - normal_speed) * s;
                cart.setMaxSpeed(speed);

            } else if (!slow_flag(cart)) {
                cart.setMaxSpeed(cartMaxSpeeds.getOrDefault(cart, NORMAL_SPEED));
            }


        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleMoveSign(VehicleMoveEvent event) {
        if (event.getVehicle() instanceof Minecart) {

            Minecart cart = (Minecart) event.getVehicle();

            //搜索告示牌
            for (int xmod : xmodifier) {
                for (int ymod : ymodifier) {
                    for (int zmod : zmodifier) {

                        cartx = cart.getLocation().getBlockX();
                        carty = cart.getLocation().getBlockY();
                        cartz = cart.getLocation().getBlockZ();
                        blockx = cartx + xmod;
                        blocky = carty + ymod;
                        blockz = cartz + zmod;
                        block = cart.getWorld().getBlockAt(blockx, blocky,
                                blockz);
                        Material mat = cart.getWorld().getBlockAt(blockx, blocky, blockz).getType();

                        if (this.isSign(mat)) {
                            Sign sign = (Sign) block.getState();
                            String[] text = sign.getLines();

                            if (text[0].equalsIgnoreCase("[msp]")) {

                                if (text[1].equalsIgnoreCase("fly")) {
                                    cart.setFlyingVelocityMod(flyingmod);

                                } else if (text[1].equalsIgnoreCase("nofly")) {
                                    cart.setFlyingVelocityMod(noflyingmod);

                                } else {

                                    error = false;
                                    try {

                                        line1 = Double.parseDouble(text[1]);

                                    } catch (Exception e) {

                                        sign.setLine(1, "WRONG VALUE");
                                        sign.setLine(2, "0.0-4.0");
                                        sign.update();
                                        error = true;

                                    }
                                    if (!error) {

                                        if (0 < line1 & line1 <= 4) {

                                            cartMaxSpeeds.put(cart, 0.4D * Double.parseDouble(text[1]));
                                        } else {

                                            sign.setLine(1, "WRONG VALUE");
                                            sign.setLine(2, "0.0-4.0");
                                            sign.update();
                                        }
                                    }
                                }


                                if (text[3].equalsIgnoreCase("noslow")) {
                                    cartslow.put(cart, false);
                                } else {
                                    cartslow.put(cart, true);
                                }
                            }

                        }

                    }
                }
            }
        }
    }

    private static Block nextRail(BlockFace direction, Block block) {
        Block b = block.getRelative(direction);
        return isRail(b) ? b : null;
    }

    private static boolean isRail(Block block) {
        Material mat = block.getType();
        return mat == Material.RAIL || mat == Material.ACTIVATOR_RAIL || mat == Material.DETECTOR_RAIL || mat == Material.POWERED_RAIL;
    }


}
