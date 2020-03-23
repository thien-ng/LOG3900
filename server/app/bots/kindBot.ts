import { IDrawing } from "../interfaces/game";
import { Taunt, DisplayMode } from './taunts';
import { Side } from '../utils/Side';
import { Bot } from "./bot";

export class kindBot extends Bot {

    constructor(image: IDrawing[],
        username: string | undefined,
        hint: string | undefined,
        mode: DisplayMode | undefined,
        panoramicFirstSide: Side | undefined) {

        super(image, username, hint, mode, panoramicFirstSide);
        this.taunts = Taunt.kind;
    }

}
