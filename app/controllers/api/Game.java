package controllers.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import connectfour.controller.GameController;
import connectfour.controller.GameField;
import connectfour.model.Computer;
import connectfour.model.Human;
import connectfour.model.Player;
import connectfour.persistence.ISaveGameDAO;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private static ISaveGameDAO saveGameDAO;
    private static HashMap<String, GameController> gameSaveing = new HashMap<>();

    @BodyParser.Of(BodyParser.Json.class)
    public static Result newGameWithoutName() {
        DynamicForm requestData = form().bindFromRequest();

        Result result;
        if (requestData.field("game") != null) {

            Form.Field f =requestData.field("game");
            result = newGameWithName(f.sub("name").value());

        } else {
            result = newGameWithName(UUID.randomUUID().toString());

        }
        return result;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result newGameWithName(String gameName) {
        GameController controller = new GameController();
        Player player = new Human();
        player.setName("You");
        Player computer = new Computer(controller);
        player.setName("Computer");
        Result result = newGame(gameName, player, computer, controller);

        return result;

    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result newGame(String gameName, Player player, Player opponent, GameController c) {
        c.setPlayer(player);
        c.setOpponend(opponent);
        c.newGame();
        ObjectNode node = Json.newObject();
        ObjectNode a = node.putObject("game");
        a.put("name", gameName);
        a.putArray("game_field");
        if (!gameSaveing.containsKey(gameName)) {
            gameSaveing.put(gameName, c);
            return ok(node);
        }
        return badRequest();

    }

    public static Result dropCoin(String gameName, int column) {
        GameController c = gameSaveing.get(gameName);
        boolean success = c.dropCoinWithSuccessFeedback(column);
        ObjectNode node = Json.newObject();
        if (success)
            node.put("dropped", true);
        else
            node.put("dropped", false);

        return ok(node);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result getGameField(String gameName) {
        if (gameSaveing.containsKey(gameName)) {
            GameController controller = gameSaveing.get(gameName);
            ObjectNode node = Json.newObject();
            node.put("game",gameFieldToJsonNode(gameName, controller));
            return ok(node);

        }

        return badRequest();
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result getGameFields() {
        ObjectNode node = Json.newObject();
        ArrayNode a = node.putArray("games");
        for (Map.Entry<String, GameController> s : gameSaveing.entrySet()) {
            ObjectNode o = a.addObject();
            o.put("name", s.getKey());
            o.put("game_field",gameFieldToJsonNode(s.getKey(), s.getValue()));
        }
        return ok(node);

    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result getSavedGames() {
        List<String> saveGames = saveGameDAO.getAllSaveGames();
        JsonNode node = Json.toJson(saveGames);
        return ok(node);
    }

    public static Result loadGame(String gameName, String loadGameName) {
        GameController controller = gameSaveing.get(gameName);
        controller.loadSaveGame(gameName);
        ObjectNode node = Json.newObject();
        node.put("loaded", true);
        return ok(node);
    }

    public static Result saveGame(String gameName, String saveGameName) {
        GameController controller = gameSaveing.get(gameName);
        controller.saveGame(gameName);
        ObjectNode node = Json.newObject();
        node.put("saved", true);
        return ok(node);
    }

    private static ObjectNode gameFieldToJsonNode(String gameName, GameController c) {
        try {
            Player[][] gameField = c.getGameField().getCopyOfGamefield();
            ObjectNode node = Json.newObject();
            node.put("name", gameName);
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
