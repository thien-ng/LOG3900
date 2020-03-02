import { injectable, inject } from "inversify";
import { GameConnection } from "./game-connection.service";
import { LobbyManagerService } from "./lobby-manager.service";
import { Arena } from "./arena";

import Types from '../../types';

@injectable()
export class GameManagerService extends GameConnection {

    private arenas: Arena[];

    public constructor(@inject(Types.LobbyManagerService) private lobServ: LobbyManagerService) {
        super()

        // TODO if statement to make server compile
        if (this.arenas) {}
        if (this.lobServ) {}
    }
    
}