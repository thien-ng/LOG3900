import { IGameplayDraw, IDrawing } from "../interfaces/game";
import { DisplayMode } from '../interfaces/creator';
import { Side } from '../utils/Side';

export abstract class Bot {

    public length: number;

    protected image: IDrawing[];
    protected taunts: string[];
    protected username: string;
    protected nextStroke: number;
    protected hint: string[];
    protected nextHint: number;
    protected mode: DisplayMode;
    protected panoramicFirstSide: Side;

    constructor(image: IDrawing[],
        username: string = "BOT:bob",
        hint: string[] = [],
        mode: DisplayMode = DisplayMode.CLASSIC,
        panoramicFirstSide: Side = Side.up) {

        this.username = username;
        this.mode = mode;
        this.image = image;
        this.length = this.image.length;
        this.hint = hint;
        this.nextHint = 0;
        this.panoramicFirstSide = panoramicFirstSide;
        this.nextStroke = 0;
        this.sort();
    }

    public GetNextStroke(): IGameplayDraw {
        if (this.isDone()) {
            throw Error("image already drawn. \n the length is a public parameter");
        }
        const out: any = this.image[this.nextStroke++];         // whacky stuff here
        out.username = this.username;                           // whacky stuff here
        const res: IGameplayDraw = out;                         // whacky stuff here
        return res;   // retourne true si il reste des traits a ajouter.
    }

    protected isDone(): boolean {
        return (this.nextStroke == this.image.length);
    }

    protected sort(): void {
        switch (this.mode) {
            case DisplayMode.CENTERED:
                this.sortCentered();
                break;
            case DisplayMode.RANDOM:
                this.sortRand();
                break;
            case DisplayMode.PANORAMIC:
                this.sortPanoramic();
                break;
        }
    }

    protected sortPanoramic(): void { //bubble sort
        for (let i = 0; i < this.image.length - 1; i++) {
            for (let j = 0; j < this.image.length - i - 1; j++) {
                if (this.comparePanoramic(j, j + 1)) {
                    this.swapStroke(j, j + 1);
                }
            }
        }
    }

    protected comparePanoramic(a: number, b: number): boolean {
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

    protected sortCentered(): void { // bubble sort
        const center = this.findCenter();
        for (let i = 0; i < this.image.length - 1; i++) {
            for (let j = 0; j < this.image.length - i - 1; j++) {
                if (this.squaredDistance(j, center.x, center.x) > this.squaredDistance(j + 1, center.x, center.x)) {
                    this.swapStroke(j, j + 1);
                }
            }
        }
    }

    protected findCenter(): { x: number, y: number } { // we are looking for an approximate value
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

    protected squaredDistance(a: number, centerX: number, centerY: number): number {
        return Math.pow(Math.min(Math.abs(this.image[a].startPosX - centerX), Math.abs(this.image[a].endPosX - centerX)), 2) +
            Math.pow(Math.min(Math.abs(this.image[a].startPosY - centerY), Math.abs(this.image[a].endPosY - centerY)), 2);
    }

    protected sortRand(): void {
        for (let i = 0; i < this.image.length - 1; i++) {
            this.swapStroke(i, i + Math.floor(Math.random() * (this.taunts.length - i)));
        }
    }

    protected swapStroke(a: number, b: number): void {
        const temp: IDrawing = this.image[b];
        this.image[b] = this.image[a];
        this.image[a] = temp;
    }

    public getHint(): string {
        return (this.nextHint >= this.hint.length) ? "No more hint available" : this.hint[this.nextHint++];
    }

    public launchTaunt(): string {
        return this.taunts[Math.floor(Math.random() * this.taunts.length)]; //ceci ou la fonction qui envoie un message avec Username
    }
}
