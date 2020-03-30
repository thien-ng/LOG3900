# Controller

## Account

| Description          | type | path                         |
| -------------------- | :--: | ---------------------------- |
| Register account     | POST | /account/register            |
| Login account        | POST | /account/login               |
| Get all users online |  GET | /account/users/online        |
| Get user information |  GET | /account/user/info/:username |

## Chat

| Description                         |  type  | path                                    |
| ----------------------------------- | :----: | --------------------------------------- |
| Get messages from by channel id     |   GET  | /chat/messages/:id                      |
| Get channels by search pattern      |   GET  | /chat/channels/search/:word             |
| Get channels not subscribed by user |   GET  | /chat/channels/notsub/:username         |
| Get channels subcribed by user      |   GET  | /chat/channels/sub/:username            |
| Join a channel                      |   PUT  | /chat/channels/join/:username/:channel  |
| Leave a channel                     | DELETE | /chat/channels/leave/:username/:channel |
| Invite player to channel            |  POST  | /chat/channels/invite                   |

## Game

| Description                  | type | path                         |
| ---------------------------- | :--: | ---------------------------- |
| Join/Create lobby            | POST | /game/lobby/join             |
| Leave/Delete lobby           | POST | /game/lobby/leave            |
| invite a user in a lobby     | POST | /game/lobby/invite           |
| invite a user in a lobby     | POST | /game/lobby/invite/refuse    |
| Get active lobbies by mode   |  GET | /game/lobby/active/:mode     |
| Get users in a lobby by name |  GET | /game/lobby/users/:lobbyName |
| Start game with lobby name   |  GET | /game/start/:lobbyName       |

## Creator

| Description                     | type | path                     |
| ------------------------------- | :--: | ------------------------ |
| Get game suggestion (assiste 2) |  GET | /creator/game/suggestion |
| Create new game                 | POST | /creator/game/new        |

# Socket

## Lobby

| Event       | Description                                                      | Object                                                                              |
| ----------- | ---------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| lobby-chat  | emit when sending messages in lobby                              |                                                                                     |
| lobby-notif | emit when sending notification about lobby update when is join   | {type: join, lobbyName: string, user: string}                                       |
| lobby-notif | emit when sending notification about lobby update when is leave  | {type: leave, lobbyName: string, user: string}                                      |
| lobby-notif | emit when sending notification about lobby update when is create | {type: create, lobbyName: string, users: string\[], private: boolean, size: number} |
| lobby-notif | emit when sending notification about lobby update when is delete | {type: delete, lobbyName: string}                                                   |

| Event          | Description                                            | Object                                                                                        |
| -------------- | ------------------------------------------------------ | --------------------------------------------------------------------------------------------- |
| login          | emit when logged in                                    |                                                                                               |
| logout         | emit when logging out                                  |                                                                                               |
| chat           | emit when sending chat messages                        |                                                                                               |
| channel-update | emit when a channel is created or deleted (depecrated) |                                                                                               |
| channel-new    | emit when a new channel is created                     |                                                                                               |
| game-start     | emit when game is started                              | N/A                                                                                           |
| draw           | emit when sending drawings to clients                  | {startPosX: number,startPosY :number,endPosX:number,endPosY:number,color:number,width:number} |
| game-chat      | emit when sending to game chat                         | {username: string, content: string, isServer:  boolean}                                       |
| drawer-update  | emit when changing roles of players in arena           | username: string                                                                              |
| game-over      | emit when it's end of the game                         | [{username: string, points: number}, {...}, ...]                                              |
