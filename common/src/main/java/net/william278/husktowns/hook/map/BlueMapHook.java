/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.hook.map;

import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.hook.MapHook;
import net.william278.husktowns.hook.PluginHook;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public final class BlueMapHook extends MapHook {

    private Map<String, MarkerSet> markerSets;

    @PluginHook(id = "BlueMap", register = PluginHook.Register.ON_ENABLE, platform = "common")
    public BlueMapHook(@NotNull HuskTowns plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        BlueMapAPI.onEnable(api -> {
            clearAllMarkers();

            this.markerSets = new HashMap<>();
            for (World world : plugin.getWorlds()) {
                getMapWorld(world).ifPresent(mapWorld -> {
                    final MarkerSet markerSet = MarkerSet.builder()
                        .label(plugin.getSettings().getGeneral().getWebMapHook().getMarkerSetName())
                        .build();
                    for (BlueMapMap map : mapWorld.getMaps()) {
                        map.getMarkerSets().put(plugin.getKey(map.getId()).toString(), markerSet);
                    }
                    markerSets.put(world.getName(), markerSet);
                });
            }

            plugin.log(Level.INFO, "Crated MarketSets:");
            for (Map.Entry<String, MarkerSet> entry : markerSets.entrySet()) {
                plugin.log(Level.INFO, "- " + entry.getKey() + " = " + entry.getValue().getMarkers().size());
            }

            plugin.populateMapHook();
        });
    }

    @NotNull
    private ShapeMarker getClaimMarker(@NotNull Town town, @NotNull Vector2d... points) {
        return ShapeMarker.builder()
            .label(town.getName())
            .fillColor(new Color(
                town.getDisplayColor().red(),
                town.getDisplayColor().green(),
                town.getDisplayColor().blue(),
                0.5f
            ))
            .lineColor(new Color(
                town.getDisplayColor().red(),
                town.getDisplayColor().green(),
                town.getDisplayColor().blue(),
                1f
            ))
            .shape(new Shape(points), 64)
            .lineWidth(1)
            .depthTestEnabled(false)
            .build();
    }

    @NotNull
    private String getClaimMarkerKey(@NotNull Town town, int count) {
        return plugin.getKey(
            Integer.toString(town.getId()),
            Integer.toString(count)
        ).toString();
    }

    @Override
    public void setClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        reloadClaimMarkers(claim.town(), world);
    }

    @Override
    public void setClaimMarkers(@NotNull List<TownClaim> claims, @NotNull World world) {
        final Map<Integer, List<TownClaim>> shapes = new HashMap<>();
        for (TownClaim claim : claims) {
            if (!shapes.containsKey(claim.town().getId())) {
                shapes.put(claim.town().getId(), new ArrayList<>());
            }
            shapes.get(claim.town().getId()).add(claim);
        }
        for (Map.Entry<Integer, List<TownClaim>> entry : shapes.entrySet()) {
            setClaimMarkers(new Shape2d(entry.getValue().get(0).town()).addAll(entry.getValue()), world);
        }
    }

    public void setClaimMarkers(@NotNull Town town, @NotNull World world) {
        plugin.getClaimWorld(world).ifPresent(
                claimWorld -> setClaimMarkers(new Shape2d(town).addAll(claimWorld.getTownClaims(town.getId(), plugin)), world)
        );
    }

    public void setClaimMarkers(@NotNull Shape2d shape, @NotNull World world) {
        getMarkerSet(world).ifPresent(markerSet -> {
            int count = 0;
            for (TownArea area : shape.getAreas()) {
                markerSet.put(getClaimMarkerKey(shape.town(), count), getClaimMarker(shape.town(), area.getPoints()));
                count++;
            }
        });
    }

    @Override
    public void removeClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        reloadClaimMarkers(claim.town(), world);
    }

    @Override
    public void removeClaimMarkers(@NotNull Town town) {
        if (markerSets != null) {
            final String id = "husktowns:" + town.getId() + "/";
            for (MarkerSet markerSet : markerSets.values()) {
                markerSet.getMarkers().entrySet().removeIf(entry -> entry.getKey().startsWith(id));
            }
        }
    }

    @Override
    public void removeClaimMarkers(@NotNull List<TownClaim> claims, @NotNull World world) {
        final Map<Integer, Town> towns = new HashMap<>();
        for (TownClaim claim : claims) {
            towns.put(claim.town().getId(), claim.town());
        }
        for (Map.Entry<Integer, Town> entry : towns.entrySet()) {
            reloadClaimMarkers(entry.getValue(), world);
        }
    }

    public void reloadClaimMarkers(@NotNull Town town, @NotNull World world) {
        final String id = "husktowns:" + town.getId() + "/";
        getMarkerSet(world).ifPresent(markerSet -> markerSet.getMarkers().entrySet().removeIf(entry -> entry.getKey().startsWith(id)));

        setClaimMarkers(town, world);
    }

    @Override
    public void clearAllMarkers() {
        if (markerSets != null) {
            for (MarkerSet markerSet : markerSets.values()) {
                for (String markerId : markerSet.getMarkers().keySet()) {
                    markerSet.remove(markerId);
                }
            }
        }
    }

    @NotNull
    private Optional<MarkerSet> getMarkerSet(@NotNull World world) {
        return markerSets == null ? Optional.empty() : Optional.ofNullable(markerSets.get(world.getName()));
    }

    @NotNull
    private Optional<BlueMapWorld> getMapWorld(@NotNull World world) {
        return BlueMapAPI.getInstance().flatMap(api -> api.getWorld(world.getName()));
    }

    public static class TownArea extends Area {

        public TownArea(@NotNull java.awt.Shape shape) {
            super(shape);
        }

        public boolean isAdjacent(@NotNull List<Vector2d> vectors) {
            final double tolerance = 0.01;
            int count = 0;
            for (Vector2d vector : vectors) {
                Rectangle2D rectAroundPoint = new Rectangle2D.Double(
                        vector.getX() - tolerance / 2,
                        vector.getY() - tolerance / 2,
                        tolerance,
                        tolerance
                );
                if (this.intersects(rectAroundPoint)) {
                    count++;
                }
            }

            return count >= 2;
        }

        @NotNull
        public Vector2d[] getPoints() {
            final List<Vector2d> vectors = new ArrayList<>();
            final PathIterator pathIterator = this.getPathIterator(null);
            final double[] coords = new double[6];

            while (!pathIterator.isDone()) {
                int segmentType = pathIterator.currentSegment(coords);

                if (segmentType == PathIterator.SEG_MOVETO || segmentType == PathIterator.SEG_LINETO) {
                    vectors.add(new Vector2d(coords[0], coords[1]));
                }
                pathIterator.next();
            }
            return vectors.toArray(new Vector2d[0]);
        }
    }

    public static class Shape2d {

        private final Town town;
        private final List<TownArea> areas = new ArrayList<>();

        public Shape2d(@NotNull Town town) {
            this.town = town;
        }

        @NotNull
        public Town town() {
            return town;
        }

        @NotNull
        @Contract("_ -> this")
        public Shape2d add(@NotNull TownClaim claim) {
            final List<Vector2d> vectors = new ArrayList<>();
            final Chunk chunk = claim.claim().getChunk();
            final int x = chunk.getX() * 16;
            final int y = chunk.getZ() * 16;
            final int x2 = x + 16;
            final int y2 = y + 16;
            vectors.add(new Vector2d(x, y));
            vectors.add(new Vector2d(x2, y));
            vectors.add(new Vector2d(x2, y2));
            vectors.add(new Vector2d(x, y2));
            return add(x, y, vectors);
        }

        @NotNull
        @Contract("_, _, _ -> this")
        public Shape2d add(int x, int y, @NotNull List<Vector2d> vectors) {
            int index = -1;
            boolean save = true;
            for (int i = 0; i < this.areas.size(); i++) {
                final TownArea area = this.areas.get(i);
                if (area.isAdjacent(vectors)) {
                    save = false;
                    if (index >= 0) {
                        this.areas.remove(i);
                        this.areas.get(index).add(area);
                        i--;
                    } else {
                        index = i;
                        area.add(new Area(new Rectangle(x, y, 16, 16)));
                    }
                }
            }

            if (save) {
                this.areas.add(new TownArea(new Rectangle(x, y, 16, 16)));
            }
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Shape2d addAll(@NotNull List<TownClaim> claims) {
            for (TownClaim claim : claims) {
                add(claim);
            }
            return this;
        }

        @NotNull
        public List<TownArea> getAreas() {
            return areas;
        }
    }

}
