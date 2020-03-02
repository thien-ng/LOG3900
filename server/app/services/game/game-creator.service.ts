import { injectable } from "inversify";

const quickDraw = require("quickdraw.js");

@injectable()
export class GameCreatorService {

    public async getSuggestion() {
        await quickDraw.import('broccoli', 1);
        // console.log("value :", quickDraw.set(1 ,["broccoli"]));
        
        return quickDraw.set(1 ,["broccoli"]).set[0].input;
    }

}