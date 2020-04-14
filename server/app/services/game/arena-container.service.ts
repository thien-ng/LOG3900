import { injectable } from "inversify";
import { ArenaFfa } from "./arena-ffa";
import { ArenaSprint } from "./arena-sprint";

@injectable()
export class ArenaContainerService {

    public arenas: Map<number, ArenaFfa | ArenaSprint>;

    public constructor() {}

    public setArenaMap(map: Map<number, ArenaFfa | ArenaSprint>): void {
        this.arenas = map;
    }
}