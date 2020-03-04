import { injectable } from 'inversify';
import { Collection, MongoClient, MongoClientOptions } from 'mongodb';
import { IGameCard } from "../interfaces/card";

import 'reflect-metadata';

const DATABASE_URL = 'mongodb+srv://admin:admin@cluster0-bt4kd.mongodb.net/test?retryWrites=true&w=majority';
const DATABASE_NAME = 'database';
const DATABASE_COLLECTION = 'cards';

@injectable()
export class CardsDbService {

    private collection: Collection<IGameCard>;

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

    public async getCards(): Promise<IGameCard[]> {
        return await this.collection.find({}).toArray();
    }

    public async deleteCard(gameID: string): Promise<void> {
        this.collection.deleteOne({gameID: gameID})
            .then()
            .catch(e => {throw e});
    } 

    public async addCard(card: IGameCard): Promise<void> {
        if (this.validateCard(card)) {
            this.collection.insertOne(card).catch(e => {
                throw e;
            });
        } else {
            throw new Error("Invalid");
        }
    }

    private validateCard(card: IGameCard): boolean {
        // TODO add vallidation of card before adding to db
        return true
    }

}