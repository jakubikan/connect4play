package controllers.api;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Injector;
import connectfour.controller.GameController;
import connectfour.controller.IController;
import connectfour.model.Computer;
import connectfour.model.Human;
import connectfour.model.Player;
import connectfour.model.SaveGame;
import connectfour.persistence.ISaveGameDAO;
import org.apache.commons.lang3.ArrayUtils;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static play.data.Form.form;


/**
 * Created with IntelliJ IDEA.
 * User: jakub
 * Date: 10/21/13
 * Time: 11:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class Game extends Controller {

    @Inject
    private Injector injector;
    @Inject
    private ISaveGameDAO saveGameDAO;

    @Inject
    private Map<String, GameController> gamesMap = new ConcurrentHashMap<>();

    @BodyParser.Of(BodyParser.Json.class)
    public Result newGameWithoutName() {
        DynamicForm requestData = form().bindFromRequest();
        Result result;
        if (requestData.field("game") != null) {

            Form.Field f = requestData.field("game");
            result = newGameWithName(f.sub("id").value());

        } else {
            result = newGameWithName(UUID.randomUUID().toString());

        }
        return result;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result newGameWithName(String gameName) {
        IController controller = null;
        try {
            controller = GameController.class.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println(controller);
        Player player = new Human();
        player.setName("You");
        Player computer = new Computer((GameController)controller);
        player.setName("Computer");
        Result result = newGame(gameName, player, computer, (GameController) controller);

        return result;

    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result newGame(String gameName, Player player, Player opponent, GameController c) {
        c.setPlayer(player);
        c.setOpponend(opponent);
        c.newGame();
        GameController controller = c;
        if (gamesMap.containsKey(gameName)) {
            controller = gamesMap.get(gameName);
        }
        ObjectNode node = Json.newObject();
        gamesMap.put(gameName, controller);
        ObjectNode a = node.putObject("game");
        a.put("id", gameName);
        a.putArray("game_field");
        return ok(node);
    }

    public Result dropCoin(String gameName, int column) {
        ObjectNode node = Json.newObject();
        if (gamesMap.containsKey(gameName)) {
            GameController controller = gamesMap.get(gameName);
            boolean success = controller.dropCoinWithSuccessFeedback(column);
            if (success)
                node.put("dropped", true);
            else
                node.put("dropped", false);
        } else {
            node.put("dropped", false);
        }


        return ok(node);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getGameField(String gameName) {
        if (gamesMap.containsKey(gameName)) {
            GameController controller = gamesMap.get(gameName);
            ObjectNode node = Json.newObject();
            node.put("game", gameFieldToJsonNode(gameName, controller));
            return ok(node);

        }
        return badRequest();
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getGameFields() {
        ObjectNode node = Json.newObject();
        ArrayNode a = node.putArray("games");
        for (Map.Entry<String, GameController> s : gamesMap.entrySet()) {
            a.add(gameFieldToJsonNode(s.getKey(), s.getValue()));
        }
        return ok(node);

    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getSavedGames() {
        Collection<GameController> gc = gamesMap.values();
        GameController controller = gc.iterator().next();
        List<String> saveGames = controller.getAllSaveGameNames();
        JsonNode node = Json.toJson(saveGames);
        return ok(node);
    }

    public Result loadGame(String gameName, String loadGameName) {
        if (gamesMap.containsKey(gameName)) {
            GameController controller = gamesMap.get(gameName);
            SaveGame sg = saveGameDAO.loadSaveGame(loadGameName);
            controller.setGameField(sg.getGameField());
            ObjectNode node = Json.newObject();
            node.put("loaded", true);
            return ok(node);
        }
        return badRequest();
    }

    public Result saveGame(String gameName, String saveGameName) throws CloneNotSupportedException {
        System.out.println(gameName);
        System.out.println(saveGameName);
        if (gamesMap.containsKey(gameName)) {
            GameController controller = gamesMap.get(gameName);
            SaveGame sg = new SaveGame(saveGameName, controller.getGameField().clone(), controller.getPlayer(), controller.getOpponend());
            saveGameDAO.saveGame(sg);
            ObjectNode node = Json.newObject();
            node.put("saved", true);
            return ok(node);
        } else {
            return badRequest();
        }
    }

    public Result undo(String gameName){
        if (gamesMap.containsKey(gameName)) {
            GameController controller = gamesMap.get(gameName);
            controller.undoStep();
            ObjectNode node = Json.newObject();
            node.put("undone", true);
            return ok(node);
        }
        return badRequest();

    }
    public Result redo(String gameName){
        if (gamesMap.containsKey(gameName)) {
            GameController controller = gamesMap.get(gameName);
            controller.redoStep();
            ObjectNode node = Json.newObject();
            node.put("undone", true);
            return ok(node);
        }
        return badRequest();

    }


    private ObjectNode gameFieldToJsonNode(String gameName, IController c) {
        try {
            Player[][] gameField = c.getGameField().getCopyOfGamefield();
            ArrayUtils.reverse(gameField);
            ObjectNode node = Json.newObject();
            node.put("id", gameName);
            ArrayNode gameArrayNode = node.putArray("game_field");
            for (Player[] rows : gameField) {
                ArrayNode rowsNode = gameArrayNode.addArray();
                for (Player p : rows) {
                    if (p != null) {
                        if (p == c.getOpponend())
                            rowsNode.add("opponent");
                        else
                            rowsNode.add("you");
                    } else {
                        rowsNode.addNull();
                    }
                }
            }
            return node;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }


}
