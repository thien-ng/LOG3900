import { IDrawing } from "../../../interfaces/game";
import { DisplayMode } from '../../../interfaces/creator';
import { Side } from '../../../utils/Side';
import { IGameplayAnnouncement } from '../../../interfaces/game';

import * as io from 'socket.io';

export abstract class Bot {

    public length: number;
    public username: string;

    protected drawings: IDrawing[];
    protected taunts: string[];
    protected nextStroke: number;
    protected mode: DisplayMode;
    protected panoramicFirstSide: Side;

    protected socket: io.Server;

    constructor(socket: io.Server, username: string) {
        this.username = username;
        this.nextStroke = 0;
        this.socket = socket;
    }

    public abstract launchTauntStart(room: string): void;

    public draw(room: string, arenaTime: number, drawings: IDrawing[], mode: DisplayMode, panoramicFirstSide: Side): NodeJS.Timeout {
        this.setupOnDraw(drawings, mode, panoramicFirstSide);

        let count = 0;
        const interval = setInterval(() => {

            if (count <= this.drawings.length) {
                const nextStroke = this.getNextStroke();
                if (nextStroke)
                    this.socket.to(room).emit("draw", nextStroke);
            } else {
                clearInterval(interval);
            }
            count++;

        }, this.calculateTimeToDrawingProportion(arenaTime));

        return interval;
    }

    private setupOnDraw(drawings: IDrawing[], mode: DisplayMode, panoramicFirstSide: Side): void {
        this.mode = mode;
        this.drawings = drawings;
        this.length = drawings.length;
        this.panoramicFirstSide = panoramicFirstSide;
        this.sort();
    }

    private calculateTimeToDrawingProportion(arenaTime: number): number {
        const proportion = this.drawings.length / (arenaTime + 5) //+5 to finish drawings early
        return 1 / proportion;
    }

    public getNextStroke(): IDrawing | undefined {
        if (this.isDone()) {
            return;
        }
        return this.drawings[this.nextStroke++];
    }

    protected isDone(): boolean {
        return (this.nextStroke == this.drawings.length);
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
        for (let i = 0; i < this.drawings.length - 1; i++) {
            for (let j = 0; j < this.drawings.length - i - 1; j++) {
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
                return Math.min(this.drawings[b].startPosY, this.drawings[b].endPosY) < Math.min(this.drawings[a].startPosY, this.drawings[a].endPosY);
            case Side.down:
                return Math.max(this.drawings[b].startPosY, this.drawings[b].endPosY) > Math.max(this.drawings[a].startPosY, this.drawings[a].endPosY);
            case Side.left:
                return Math.min(this.drawings[b].startPosX, this.drawings[b].endPosX) < Math.min(this.drawings[a].startPosX, this.drawings[a].endPosX);
            case Side.right:
                return Math.max(this.drawings[b].startPosX, this.drawings[b].endPosX) > Math.max(this.drawings[a].startPosX, this.drawings[a].endPosX);
        }
        return false;
    }

    protected sortCentered(): void { // bubble sort
        const center = this.findCenter();
        for (let i = 0; i < this.drawings.length - 1; i++) {
            for (let j = 0; j < this.drawings.length - i - 1; j++) {
                if (this.squaredDistance(j, center.x, center.y) > this.squaredDistance(j + 1, center.x, center.y)) {
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
        for (let i = 0; i < this.drawings.length; i++) {
            smallX = (this.drawings[i].startPosX < smallX) ? this.drawings[i].startPosX : smallX;
            smallY = (this.drawings[i].startPosY < smallY) ? this.drawings[i].startPosY : smallX;
            bigX = (this.drawings[i].startPosX > bigX) ? this.drawings[i].startPosX : bigX;
            bigY = (this.drawings[i].startPosY > bigY) ? this.drawings[i].startPosY : bigY;
        }

        return { x: (smallX + bigX) / 2, y: (smallY + bigY) / 2 };
    }

    protected squaredDistance(a: number, centerX: number, centerY: number): number {
        return Math.pow(Math.min(Math.abs(this.drawings[a].startPosX - centerX), Math.abs(this.drawings[a].endPosX - centerX)), 2) +
            Math.pow(Math.min(Math.abs(this.drawings[a].startPosY - centerY), Math.abs(this.drawings[a].endPosY - centerY)), 2);
    }

    protected sortRand(): void {
        for (let i = 0; i < this.drawings.length - 1; i++) {
            this.swapStroke(i, i + Math.floor(Math.random() * (this.taunts.length - i)));
        }
    }

    protected swapStroke(a: number, b: number): void {
        const temp: IDrawing = this.drawings[b];
        this.drawings[b] = this.drawings[a];
        this.drawings[a] = temp;
    }

    public launchTaunt(room: string): void {
        const taunt = this.taunts[Math.floor(Math.random() * this.taunts.length)]; //ceci ou la fonction qui envoie un message avec Username
        const announcement: IGameplayAnnouncement = {
            username: this.username,
            content: taunt,
            isServer: false,
        };
        this.socket.to(room).emit("game-chat", announcement);
    }
}
