package com.vitco.objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

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
                    if (Math.abs(pos[i][0] - pos[j][0]) <= Entity.screenWidth * Entity.bufferParameter &&
                            Math.abs(pos[i][1] - pos[j][1]) <= Entity.screenHeight * Entity.bufferParameter) {
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

    // test random moving, adding and removing
    @org.junit.Test
    public void testPlayerEvents() throws Exception {
        for (int j = 0; j < 100; j++) {
            // make sure the entity world is clean
            World world = new World();
            // create players
            ArrayList<Player> players = new ArrayList<Player>();
            for (int i = 0; i < 3000; i++) {
                switch(rand.nextInt(3)) {
                    case 0: // add
                        players.add(new Player(0, 0, world));
                        break;
                    case 1: // remove
                        if (!players.isEmpty()) {
                            Player removed = players.remove(rand.nextInt(players.size()));
                            removed.destroyEntity();
                        }
                        break;
                    case 2: case 3: // move
                        if (!players.isEmpty()) {
                            Player player = players.get(rand.nextInt(players.size()));
                            Point pos = player.getPosition();
                            player.setPosition(pos.x + rand.nextInt(501) - 250, pos.y + rand.nextInt(501) - 250);
                        }
                        break;
                }
            }
            // check
            checkPlayerVisibilityList(convert(players));
        }
    }

    // test player invisible and visible lists
    @org.junit.Test
    public void testPlayerLists() throws Exception {
        // make sure whe have a clean world
        World world = new World();
        // create player
        Player playerA = new Player(0, 0, world);
        Player playerB = new Player(
                (int)(Entity.screenWidth * Entity.bufferParameter) + 10,
                (int)(Entity.screenHeight * Entity.bufferParameter) + 10, world);
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
        playerB.setPosition((int)(Entity.screenWidth * Entity.bufferParameter) + 10,
                (int)(Entity.screenHeight * Entity.bufferParameter) + 10);
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
}




























