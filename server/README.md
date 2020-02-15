# Controller
## Account
| Description           | type | path                   |
| --------------------- |:----:|----------------------- |
| Register account      |POST  | /account/register      |
| Login account         |POST  | /account/login         |
| Get all users online  |GET   | /account/users/online  |

## Chat
| Description                       | type | path                                   |
| ---------------------             |:----:|-----------------------                 |
| Get messages from by channel id   |GET   | /chat/messages/:id                     |
| Get all existing channels         |GET   | /chat/channels/all                     |
| Get channels subcribed by user    |GET   | /chat/channels/:username               |   
| Join a channel                    |PUT   | /chat/channels/join/:username/:channel |
| Leave a channel                   |DELETE| /chat/channels/leave/:username/:channel|


# Socket
|Event  | Description                       |
|-----  | -----------                       |
|login  | emit when logged in               |
|logout | emit when logging out             |
|chat   | emit when sending chat messages   |