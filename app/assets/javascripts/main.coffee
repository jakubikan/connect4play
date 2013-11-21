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

}


Connectfour.IndexRoute = Ember.Route.extend {
  model: () ->
    @transitionTo "games.index"

}

Connectfour.GamesRoute = Ember.Route.extend {
  model: () ->
    @store.find "game"

}

Connectfour.GamesIndexRoute = Ember.Route.extend {
  model: () ->
    @store.find "game"

}


Connectfour.GamesNewRoute = Ember.Route.extend {
  model: (params)->
    @store.createRecord "game", params

  setupController: ( controller, model ) ->
    route = this

    model = @modelFor("gamesNew").save().then((params) ->
      route.transitionTo('games.play');
    , ->
      console.log "failing save", this
    )

}



Connectfour.GamesPlayRoute = Ember.Route.extend {
  model: (params) ->
    @store.find "game", params.id
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

Connectfour.GameRoute = Ember.Route.extend {
  model: (params) ->
    @store.find "game", params.name

}

















