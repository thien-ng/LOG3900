import { expect } from 'chai';
import * as inversify from 'inversify';
import Types from '../types';
import { IndexService} from './index.service';
import { DateService } from './date.service';
import { Message } from '../../../common/communication/message';

class MockDateService extends DateService{
    async currentTime(): Promise<Message> {
        return {
            title: `Time`,
            body: new Date(2020, 0, 10).toString(),
        };
    }
}

class MockErrorDateService extends DateService{
    async currentTime(): Promise<Message> {
        throw new Error(`error in the service`);
    }
}

describe('Index service', () => {

    let indexService: IndexService;
    let container: inversify.Container;

    beforeEach(()=>{
        container = new inversify.Container();
        container.bind(Types.IndexService).to(IndexService);
        container.bind(Types.DateService).to(MockDateService);
        indexService = container.get<IndexService>(Types.IndexService);
    })

    it('should return Hello World as title', (done: Mocha.Done) => {
        indexService.helloWorld().then((result:Message)=>{
            expect(result.title).to.equals(`Hello world`);
            done();
        });     
    });

    it('should have a body that starts with \'Time is\'',(done: Mocha.Done)=>{
        indexService.helloWorld().then((result:Message)=>{
            expect(result.body).to.be.a('string').and.satisfy((body:string) => body.startsWith('Time is'));
            done();
        });
    });

    it('should handle an error from DateService',(done: Mocha.Done)=>{
        // Replace with the another mock
        container.unbind(Types.DateService);
        container.bind(Types.DateService).to(MockErrorDateService);
        indexService = container.get<IndexService>(Types.IndexService);

        indexService.helloWorld().then((result:Message)=>{
            expect(result.title).to.equals('Error');
            done();
        }).catch((error:unknown)=>{ 
            done(error);
        });
    })
});
