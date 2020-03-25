import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import { LobbyManagerService } from '../services/game/lobby-manager.service';
import { GameManagerService } from '../services/game/game-manager.service';

import Types from '../types';
import { GameMode } from '../interfaces/game';

@injectable()
export class GameController {
    public router: Router;

    public constructor(
        @inject(Types.LobbyManagerService) private lobbyServ: LobbyManagerService,
        @inject(Types.GameManagerService) private gameMan: GameManagerService) {
        this.configureRouter();
    }

    private configureRouter() {
        this.router = Router();

        this.router.post('/lobby/join', (req: Request, res: Response, next: NextFunction) => {
            res.json(this.lobbyServ.join(req.body));
        });

        this.router.post('/lobby/invite', (req: Request, res: Response, next: NextFunction) => {
            res.json(this.lobbyServ.invite(req.body.lobbyName, req.body.username));
        }); // need to verify identity (should be through socket ?)

        this.router.post('/lobby/leave', (req: Request, res: Response, next: NextFunction) => {
            res.json(this.lobbyServ.leave(req.body));
        });

        this.router.get('/lobby/active/:mode', (req: Request, res: Response, next: NextFunction) => {
            res.json(this.lobbyServ.getActiveLobbies(req.params.mode as GameMode));
        });

        this.router.get('/lobby/users/:lobbyName', (req: Request, res: Response, next: NextFunction) => {
            res.json(this.lobbyServ.getUsersInLobby(req.params.lobbyName));
        });

        this.router.get('/start/:lobbyName', (req: Request, res: Response, next: NextFunction) => {
            this.gameMan.startGame(req.params.lobbyName);
            res.status(200).send();
        });
    }
}
