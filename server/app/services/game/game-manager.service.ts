import { injectable, inject } from "inversify";
import { GameConnection } from "./game-connection.service";
import { LobbyManagerService } from "./lobby-manager.service";
import { IGameStartData } from "../../interfaces/game";
import { Arena } from "./arena";

import Types from '../../types';

@injectable()
export class GameManagerService extends GameConnection {

    private arenas: Arena[];

    public constructor(@inject(Types.LobbyManagerService) private lobServ: LobbyManagerService) {
        super()
    }

    public startGame(data: IGameStartData): void {
        const lobby = this.lobServ.lobbies.get(data.lobbyName);

        if (!lobby)
            throw new Error(`lobby ${data.lobbyName} does not exists`);

    }
    
}