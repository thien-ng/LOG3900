import { injectable, inject } from "inversify";
import { IGameplayDraw, IDrawing } from "./interfaces/game";
import { GameManagerService } from "./services/game/game-manager.service";
import Types from './types';

@injectable()
export class Bot {

    private username: string;
    private image: IDrawing[];
    private nextStroke: number;

    constructor(username: string, image: IDrawing[], @inject(Types.GameManagerService) private gameManager: GameManagerService) {

        this.username = username;
        this.image = image;
        this.findStroke();
    }

    public sendNextStroke(): void {
        const out: any = this.image[this.nextStroke];           // whacky stuff here
        out.username = this.username;                           // whacky stuff here
        const res: IGameplayDraw = out;                         // whacky stuff here
        this.gameManager.sendMessageToArena(res);               // ceci ou la fonction qui enverra a tout le monde
        this.findStroke();
    }

    private findStroke(): void {
        if (this.nextStroke == null) {
            this.nextStroke = 0;
            return;
        }

        this.nextStroke++;
    }

    public d(): void {

    }

}
