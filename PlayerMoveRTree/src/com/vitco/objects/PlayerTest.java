package com.vitco.objects;

import com.infomatiq.jsi.Rectangle;
import com.vitco.util.RTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;

/**
 * Test class for player
 */
public class PlayerTest {

    // logger
    private static final Logger log = LoggerFactory.getLogger(PlayerTest.class);

    // random number generator
    private static final Random rand = new Random(1);

    // make players move around randomly
    private void movePlayersAround(Player[] players) {
        for (Player player : players) {
            Point pos = player.getPosition();
            player.setPosition(pos.x + rand.nextInt(501) - 250, pos.y + rand.nextInt(501) - 250);
        }
    }



    // checks that the player visibility list is correct by
    // testing if it's equal to a brute force list
    private void checkPlayerVisibilityList(Player[] players) {
        Integer[][] pos = new Integer[players.length][];
        for (int i = 0; i < players.length; i++) {
            Point point = players[i].getRectCenter();
            pos[i] = new Integer[]{point.x, point.y};
        }
        for (int i = 0; i < players.length; i++) {
            HashSet<Entity> visibleList = new HashSet<Entity>();
            for (int j = 0; j < players.length; j++) {
                if (i != j) {
                    if (Math.abs(pos[i][0] - pos[j][0]) <= Entity.screenWidth * Entity.bufferParameter * 1.5 &&
                            Math.abs(pos[i][1] - pos[j][1]) <= Entity.screenHeight * Entity.bufferParameter * 1.5) {
                        visibleList.add(players[j]);
                    }
                }
            }
            boolean listSameSize = visibleList.size() == players[i].getVisibleList().length;
            if (!listSameSize) {
                log.error("Lists have different size:");
                log.error(visibleList.size() + " vs " + players[i].getVisibleList().length);
            }
            for (Entity entity : players[i].getVisibleList()) {
                // debug
                boolean entryRemoved = visibleList.remove(entity);
                if (!entryRemoved) {
                    log.error("Entry was not found in brute-force visible list:");
                    log.error(players[i].entityPositionX + ", " + players[i].entityPositionY);
                    log.error(entity.entityPositionX + ", " + entity.entityPositionY);
                    log.error("Dist X: " + Math.abs(players[i].entityPositionX - entity.entityPositionX));
                    log.error("Dist Y: " + Math.abs(players[i].entityPositionY - entity.entityPositionY));
                }
                assert entryRemoved;
            }
            // debug
            boolean listEmpty = visibleList.isEmpty();
            if (!listEmpty) {
                log.error("Too many items in the brute-force visible list:");
                for (Entity entity : visibleList) {
                    log.error("===" + visibleList.size());
                    log.error("Dist X: " + Math.abs(players[i].entityPositionX - entity.entityPositionX));
                    log.error("Dist Y: " + Math.abs(players[i].entityPositionY - entity.entityPositionY));
                }
            }
            assert listEmpty;
        }
    }

    // helper - convert collection to array
    private static Player[] convert(Collection<Player> players) {
        Player[] result = new Player[players.size()];
        players.toArray(result);
        return result;
    }

    // test moving speed of players
    @org.junit.Test
    public void testPlayerMoving() throws Exception {
        for (int j = 0; j < 10; j++) {
            // make sure whe have a clean world
            World world = new World();
            // create players
            HashSet<Player> players = new HashSet<Player>();
            for (int i = 0; i < 300; i++) {
                players.add(new Player(0, 0, world));
            }
            // check visibility list
            checkPlayerVisibilityList(convert(players));
            // update location
            Player[] converted = convert(players);
            for (int i = 0; i < 20; i++) {
                movePlayersAround(converted);
            }
            // check again
            checkPlayerVisibilityList(convert(players));
        }
    }

    // helper to shuffle/create/delete players with
    // different probabilities
    private void simulatePlayers(ArrayList<Player> players, World world, int count,
                                 float add, float remove, float move) {
        float sum = add + remove + move;
        if (sum > 0) {
            add = add/sum;
            remove = remove/sum;
            move = move/sum;
        }
        for (int i = 0; i < count; i++) {
            float val = rand.nextFloat();
            if (val < add) {
                players.add(new Player(rand.nextInt(501) - 250, rand.nextInt(501) - 250, world));
            } else if (val < add + remove) {
                if (!players.isEmpty()) {
                    Player removed = players.remove(rand.nextInt(players.size()));
                    world.destroyEntity(removed);
                }
            } else if (val < add + remove + move) {
                if (!players.isEmpty()) {
                    Player player = players.get(rand.nextInt(players.size()));
                    Point pos = player.getPosition();
                    player.setPosition(pos.x + rand.nextInt(501) - 250, pos.y + rand.nextInt(501) - 250);
                }
            }
        }
    }

    // test random moving, adding and removing
    @org.junit.Test
    public void testPlayerEvents() throws Exception {
        for (int j = 0; j < 100; j++) {
            // make sure the entity world is clean
            World world = new World();
            ArrayList<Player> players = new ArrayList<Player>();
            // shuffle/create/delete players
            simulatePlayers(players, world, 1500, rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            // check
            checkPlayerVisibilityList(convert(players));
        }
    }

    // test player invisible and visible lists
    @org.junit.Test
    public void testPlayerLists() throws Exception {
        for (int j = 0; j < 100; j++) {
            // make sure the entity world is clean
            World world = new World();
            ArrayList<Player> players = new ArrayList<Player>();
            // create players
            simulatePlayers(players, world, 300, rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            // store player lists
            HashMap<Player, HashSet<Entity>> visLists = new HashMap<Player, HashSet<Entity>>();
            for (Player player : players) {
                HashSet<Entity> visList = new HashSet<Entity>();
                Collections.addAll(visList, player.getVisibleList());
                visLists.put(player, visList);
                player.clearNewInvisibleList();
                player.clearNewVisibleList();
            }
            // check the lists
            for (Player player : players) {
                // move this player
                for (int k = 0; k < 10; k++) {
                    Point pos = player.getPosition();
                    player.setPosition(pos.x + rand.nextInt(501) - 250, pos.y + rand.nextInt(501) - 250);
                }

                // analyze the lists
                HashSet<Entity> oldVisList = visLists.get(player);
                HashSet<Entity> visList = new HashSet<Entity>();
                Collections.addAll(visList, player.getVisibleList());
                HashSet<Entity> newVisList = new HashSet<Entity>();
                Collections.addAll(newVisList, player.getNewVisibleList());
                HashSet<Entity> newInvisList = new HashSet<Entity>();
                Collections.addAll(newInvisList, player.getNewInvisibleList());

                // became invisible
                HashSet<Entity> test1 = new HashSet<Entity>(oldVisList);
                test1.removeAll(visList);
                // Note: see below
                assert newInvisList.containsAll(test1);

                // became visible
                HashSet<Entity> test2 = new HashSet<Entity>(visList);
                test2.removeAll(oldVisList);
                // Note: this is a one way contains as already existing items in the
                // visible list can become invisible and visible again and would hence
                // be in the newVisibleList
                assert newVisList.containsAll(test2);

                for (Player player1 : players) {
                    HashSet<Entity> visList1 = new HashSet<Entity>();
                    Collections.addAll(visList1, player1.getVisibleList());
                    visLists.put(player1, visList1);
                    player1.clearNewInvisibleList();
                    player1.clearNewVisibleList();
                }
            }
        }
    }

    // test player invisible and visible lists
    @org.junit.Test
    public void testPlayerListsSimple() throws Exception {
        // make sure whe have a clean world
        World world = new World();
        // create player
        Player playerA = new Player(0, 0, world);
        Player playerB = new Player(
                (int)(Entity.screenWidth * Entity.bufferParameter * 1.5) + 10,
                (int)(Entity.screenHeight * Entity.bufferParameter * 1.5) + 10, world);
        assert playerA.getVisibleList().length == 0;
        assert playerB.getVisibleList().length == 0;
        assert playerA.getNewVisibleList().length == 0;
        assert playerB.getNewVisibleList().length == 0;
        assert playerA.getNewInvisibleList().length == 0;
        assert playerB.getNewInvisibleList().length == 0;
        // move the player close
        playerB.setPosition(1,1);
        assert playerA.getVisibleList().length == 1;
        assert playerB.getVisibleList().length == 1;
        assert playerA.getNewVisibleList().length == 0;
        assert playerB.getNewVisibleList().length == 1;
        assert playerA.getNewInvisibleList().length == 0;
        assert playerB.getNewInvisibleList().length == 0;
        // move player away again
        playerB.setPosition((int)(Entity.screenWidth * Entity.bufferParameter * 1.5) + 10,
                (int)(Entity.screenHeight * Entity.bufferParameter * 1.5) + 10);
        assert playerA.getVisibleList().length == 0;
        assert playerB.getVisibleList().length == 0;
        assert playerA.getNewVisibleList().length == 0;
        assert playerB.getNewVisibleList().length == 0;
        assert playerA.getNewInvisibleList().length == 0;
        assert playerB.getNewInvisibleList().length == 1;
        playerB.clearNewInvisibleList();
        assert playerB.getNewInvisibleList().length == 0;
        // move the player close
        playerB.setPosition(1,1);
        assert playerB.getNewVisibleList().length == 1;
        playerB.clearNewVisibleList();
        assert playerB.getNewVisibleList().length == 0;
    }


    // testing delete bug
    @org.junit.Test
    public void testPlayerDelete() throws Exception {
        World world = new World();
        final int size = 1000;
        ArrayList<Player> players = new ArrayList<Player>();
        for (int i = 0; i < size; i++) {
            players.add(new Player(rand.nextInt(2),rand.nextInt(2),world));
        }
        for (int i = 0; i < 10000; i++) {
            players.get(rand.nextInt(size)).setPosition(rand.nextInt(2),rand.nextInt(2));
        }
        for (int i = 0; i < size/2; i++) {
            world.destroyEntity(players.remove(rand.nextInt(players.size())));
        }
        checkPlayerVisibilityList(convert(players));
    }


    // test RTree implementation
    @org.junit.Test
    public void testRTree() throws Exception {
        RTree rTree = new RTree();
        rTree.add(new Rectangle(0,0,0,0), 1);
        rTree.add(new Rectangle(0,0,0,0), 2);
        rTree.add(new Rectangle(0,0,0,0), 3);

        rTree.delete(new Rectangle(0,0,0,0), 2);

        assert rTree.size() == 2;
    }

}




























