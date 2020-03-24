import { IDrawing } from "../interfaces/game";
import { DisplayMode } from '../interfaces/creator';
import { Taunt } from './taunts';
import { Side } from '../utils/Side';
import { Bot } from "./bot";

export class MeanBot extends Bot {

    constructor(image: IDrawing[],
        username: string | undefined,
        hint: string[] | undefined,
        mode: DisplayMode | undefined,
        panoramicFirstSide: Side | undefined) {

        super(image, username, hint, mode, panoramicFirstSide);
        this.taunts = Taunt.mean;
    }

}
