# Controller
## Account
| Description           | type | path                   |
| --------------------- |:----:|----------------------- |
| Register account      |POST  | /account/register      |
| Login account         |POST  | /account/login         |
| Get all users online  |GET   | /account/users/online  |

## Chat
| Description                           | type | path                                   |
| ---------------------                 |:----:|-----------------------                 |
| Get messages from by channel id       |GET   | /chat/messages/:id                     |
| Get channels by search pattern by name|GET   | /chat/channels/search/:username/:word? |
| Get channels not subscribed by user   |GET   | /chat/channels/notsub/:username        |
| Get channels subcribed by user        |GET   | /chat/channels/sub/:username           |   
| Join a channel                        |PUT   | /chat/channels/join/:username/:channel |
| Leave a channel                       |DELETE| /chat/channels/leave/:username/:channel|
| Invite player to channel              |POST  | /chat/channels/invite                  |

## Game
| Description                       | type | path                                   |
| ---------------------             |:----:|-----------------------                 |
| Join/Create lobby                 |POST  | /game/lobby/join                       |
| Leave/Delete lobby                |POST  | /game/lobby/leave                      |
| Get active lobbies                |GET   | /game/lobby/active                     |

# Socket
|Event      | Description                                       |
|-----      | -----------                                       |
|login      | emit when logged in                               |
|logout     | emit when logging out                             |
|chat       | emit when sending chat messages                   |
|lobby-chat | emit when sending messages in lobby               |
|lobby-notif| emit when sending notification about lobby update |