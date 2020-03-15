import { injectable } from 'inversify';
import { Collection, MongoClient, MongoClientOptions } from 'mongodb';
import { IGameRule } from "../interfaces/card";

import 'reflect-metadata';

const DATABASE_URL = 'mongodb+srv://admin:admin@cluster0-bt4kd.mongodb.net/test?retryWrites=true&w=majority';
const DATABASE_NAME = 'database';
const DATABASE_COLLECTION = 'cards';

@injectable()
export class CardsDbService {

    private collection: Collection<IGameRule>;

    private options: MongoClientOptions = {
        useNewUrlParser : true,
        useUnifiedTopology : true
    };

    public constructor() {
        MongoClient.connect(DATABASE_URL, this.options).then(client => {
            this.collection = client.db(DATABASE_NAME).collection(DATABASE_COLLECTION);            
        })
        .catch(() => {
            console.error('CONNECTION ERROR. EXITING PROCESS');
            process.exit(1);
        });
    }

    public async getCards(): Promise<IGameRule[]> {
        return await this.collection.find({}).toArray();
    }

    public async getCardByGameId(gameID: string): Promise<IGameRule | null> {
        return await this.collection.findOne({gameID: gameID});
    }

    public async deleteCard(gameName: string): Promise<void> {
        this.collection.deleteOne({gameName: gameName})
            .then()
            .catch(e => {throw e});
    } 

    public async addRule(rule: IGameRule): Promise<void> {
        if (this.validateCard(rule)) {
            this.collection.insertOne(rule).catch(e => {
                throw e;
            });
        } else {
            throw new Error("Invalid");
        }
    }

    public getRulesByGameID(gameID: string): void {
        this.collection.findOne({gameID: gameID})
        .then(IGameCard => { return IGameCard })
        .catch(e => { throw e });
    }

    private validateCard(card: IGameRule): boolean {
        // TODO add vallidation of card before adding to db
        // add validation when we know what is needed for a card
        return true
    }

}