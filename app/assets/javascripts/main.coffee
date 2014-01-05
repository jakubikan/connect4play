window.Connectfour = Ember.Application.create( {
  LOG_TRANSITIONS: true
  LOG_ACTIVE_GENERATION: true,
  LOG_MODULE_RESOLVER: true,
  LOG_TRANSITIONS: true,
  LOG_TRANSITIONS_INTERNAL: true,
  LOG_VIEW_LOOKUPS: true,

});

Connectfour.Router.reopen {
  rootURL: '/api/'
}

DS.RESTAdapter.reopen({
  namespace: 'api'
});


Connectfour.Router.map  ->
  @resource "games", ->
    @route "new", {path: "new/:id"}
    @route "play", {path: "/:id"}
    @route "dropcoin", {path: ":name/dropCoin/:column"}
    @route "save", {path: ":id/save/:saveGame"}
    @route "load", {path: ":id/load/:saveGame"}
  @route "saveGames"



Connectfour.Game = DS.Model.extend {
  game_field: DS.attr ""
  isPlayerVsPlayer: DS.attr "boolean"
  isWaitingForOpponent: DS.attr "boolean"
  gameStarted: DS.attr "boolean"
  playerOnTurn: DS.attr ""

}

Connectfour.StandartActions = Ember.Route.extend {
  actions: {
    newGame: (gameName, pvp)  ->
      params = {
        id: gameName
        isPlayerVsPlayer: pvp
      }
      @transitionTo("games.new", params);

  }
}

Connectfour.IndexRoute = Connectfour.StandartActions.extend {
  model: ->
    @transitionTo "games.index"
}


Connectfour.GamesIndexRoute = Connectfour.StandartActions.extend {
  model: ->
    @store.find "game"
  actions: {
    joinGame: (id, view) ->
      $.ajax {
        url:"/api/games/#{id}/joinGame"
        async:false
      }
      @transitionTo "games.play", id
  }
  reload: =>
    @store.find "game"


}


Connectfour.GamesNewRoute = Connectfour.StandartActions.extend {
  model: (params)->
    @store.createRecord "game", params

  setupController: ( controller, model ) ->
    route = this
    m = @modelFor("gamesNew")
    if typeof  m.save != 'function'
      if !m.isPlayerVsPlayer
        m.isPlayerVsPlayer = false;
      m = @store.createRecord "game", m
      route.transitionTo('games.play');

    model = m.save().then((params) ->
      route.transitionTo('games.play');
    , ->
      console.log "failing save", this
    )

}


Connectfour.GamePoll = {
  start: (func) ->
    @timer = setInterval func.bind(this), 2000
  stop: ->
    clearInterval @timer

};




Connectfour.GamesPlayRoute = Connectfour.StandartActions.extend {
  model: (params) ->
    @store.find "game", params.id
  setupController: (controller, model) ->
    controller.set("model", model)
    Connectfour.GamePoll.start ->
      model.reload();
    model

  actions: {
    dropCoin: (column, view) ->
      $.ajax {
        url:"/api/games/#{@currentModel.id}/dropcoin/#{column}"
        async:false
      }
      @currentModel.reload()
    save: (name)->
      $.ajax {
        url:"/api/games/#{@currentModel.id}/save/#{name}"
        async:false
      }

    load: (name)->
      $.ajax {
        url:"/api/games/#{@currentModel.id}/load/#{name}"
        async:false
      }
      @currentModel.reload()
    undo: ->
      $.ajax {
        url:"/api/games/#{@currentModel.id}/undo"
        async:false
      }
      @currentModel.reload()

    redo: ->
      $.ajax {
        url:"/api/games/#{@currentModel.id}/redo"
        async:false
      }
      @currentModel.reload()
  }

}


Connectfour.GameRoute = Connectfour.StandartActions.extend {
  model: (params) ->
    @store.find "game", params.name

}



















