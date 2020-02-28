import { injectable, inject } from "inversify";
import { GameConnection } from "./game-connection.service";
import { LobbyManagerService } from "./lobby-manager.service";

import Types from '../../types';

@injectable()
export class GameManagerService extends GameConnection {

    public constructor(@inject(Types.LobbyManagerService) private lobServ: LobbyManagerService) {
        super()
    }



}