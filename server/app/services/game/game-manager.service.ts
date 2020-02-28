import { injectable, inject } from "inversify";
import { GameConnection } from "./game-connection.service";
import { LobbyManagerService } from "./lobby-manager.service";
import { IGameStartData } from "../../interfaces/game";

import Types from '../../types';

@injectable()
export class GameManagerService extends GameConnection {

    public constructor(@inject(Types.LobbyManagerService) private lobServ: LobbyManagerService) {
        super()
    }

    public startGame(data: IGameStartData): void {
        const lobby = this.lobServ.lobbies.get(data.lobbyName);
        if (lobby) {}
        console.log(lobby);
        
    }

    
}