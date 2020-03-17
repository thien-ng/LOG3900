import { injectable, inject } from "inversify";
import { IGameplayDraw, IDrawing } from "../interfaces/game";
import { GameManagerService } from "../services/game/game-manager.service";
import Types from '../types';
import { Taunt, Personality, DisplayMode } from './taunts';
import { Side } from '../utils/Side';

@injectable()
export class Bot {

    private image: IDrawing[];
    private taunts: string[];
    private username: string;
    private nextStroke: number;
    private hint: string;
    private mode: DisplayMode;
    private panoramicFirstSide: Side;

    constructor(image: IDrawing[],
        username: string = "BOT:bob",
        hint: string = "no hint for you!",
        mode: DisplayMode = DisplayMode.classic,
        style: Personality = Personality.length,
        panoramicFirstSide: Side = Side.up,
        @inject(Types.GameManagerService) private gameManager: GameManagerService) {

        this.username = username;
        this.mode = mode;
        this.image = image;
        this.hint = hint;
        this.panoramicFirstSide = panoramicFirstSide;
        this.sort();
        this.findStroke();
        if (style == Personality.length)
            style = this.randPersonality();
        this.taunts = Taunt.getTaunts(style);
    }

    public sendNextStroke(): boolean {
        const out: any = this.image[this.nextStroke];           // whacky stuff here
        out.username = this.username;                           // whacky stuff here
        const res: IGameplayDraw = out;                         // whacky stuff here
        this.gameManager.sendMessageToArena(res);               // ceci ou la fonction qui enverra a tout le monde
        this.findStroke();
        return !this.isDone();   // retourne true si il reste des traits a ajouter.
    }

    private isDone(): boolean {
        return (this.nextStroke == this.image.length);
    }

    private findStroke(): void { // quand le dernier trait a ete envoyee, on met le prochain a image.length;
        if (this.nextStroke == null) {
            this.nextStroke = 0;
            return;
        }
        this.nextStroke++;
    }

    private sort(): void {
        switch (this.mode) {
            case DisplayMode.centered:
                this.sortCentered();
                break;
            case DisplayMode.rand:
                this.sortRand();
                break;
            case DisplayMode.panoramic:
                this.sortPanoramic();
                break;
        }
    }

    private sortPanoramic(): void { //bubble sort
        for (let i = 0; i < this.image.length - 1; i++) {
            for (let j = 0; j < this.image.length - i - 1; j++) {
                if (this.comparePanoramic(j, j + 1)) {
                    this.swapStroke(j, j + 1);
                }
            }
        }
    }

    private comparePanoramic(a: number, b: number): boolean {
        // considering if b should come before a.
        switch (this.panoramicFirstSide) {
            case Side.up:
                return Math.min(this.image[b].startPosY, this.image[b].endPosY) < Math.min(this.image[a].startPosY, this.image[a].endPosY);
            case Side.down:
                return Math.max(this.image[b].startPosY, this.image[b].endPosY) > Math.max(this.image[a].startPosY, this.image[a].endPosY);
            case Side.left:
                return Math.min(this.image[b].startPosX, this.image[b].endPosX) < Math.min(this.image[a].startPosX, this.image[a].endPosX);
            case Side.right:
                return Math.max(this.image[b].startPosX, this.image[b].endPosX) > Math.max(this.image[a].startPosX, this.image[a].endPosX);
        }
        return false;
    }

    private sortCentered(): void { // bubble sort
        const center = this.findCenter();
        for (let i = 0; i < this.image.length - 1; i++) {
            for (let j = 0; j < this.image.length - i - 1; j++) {
                if (this.squaredDistance(j, center.x, center.x) > this.squaredDistance(j + 1, center.x, center.x)) {
                    this.swapStroke(j, j + 1);
                }
            }
        }
    }

    private findCenter(): { x: number, y: number } { // we are looking for an approximate value
        let bigX = 0;
        let bigY = 0;
        let smallX = Infinity;
        let smallY = Infinity;
        for (let i = 0; i < this.image.length; i++) {
            smallX = (this.image[i].startPosX < smallX) ? this.image[i].startPosX : smallX;
            smallY = (this.image[i].startPosY < smallY) ? this.image[i].startPosY : smallX;
            bigX = (this.image[i].startPosX > bigX) ? this.image[i].startPosX : smallX;
            bigY = (this.image[i].startPosY > bigY) ? this.image[i].startPosY : smallX;
        }

        return { x: (smallX + bigX) / 2, y: (smallY + bigY) / 2 };
    }

    private squaredDistance(a: number, centerX: number, centerY: number): number {
        return Math.pow(Math.min(Math.abs(this.image[a].startPosX - centerX), Math.abs(this.image[a].endPosX - centerX)), 2) +
            Math.pow(Math.min(Math.abs(this.image[a].startPosY - centerY), Math.abs(this.image[a].endPosY - centerY)), 2);
    }

    private sortRand(): void {
        for (let i = 0; i < this.image.length - 1; i++) {
            this.swapStroke(i, i + Math.floor(Math.random() * (this.taunts.length - i)));
        }
    }

    private swapStroke(a: number, b: number): void {
        const temp: IDrawing = this.image[b];
        this.image[b] = this.image[a];
        this.image[a] = temp;
    }

    public getHint(): string {
        return this.hint;
    }

    public launchTaunt(): string {
        return this.taunts[Math.floor(Math.random() * this.taunts.length)]; //ceci ou la fonction qui envoie un message avec Username
    }

    private randPersonality(): Personality {
        return Math.floor(Math.random() * Personality.length);
    }

}
