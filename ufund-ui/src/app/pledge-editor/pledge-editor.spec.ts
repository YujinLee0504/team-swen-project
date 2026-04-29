import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PledgeEditor } from './pledge-editor';

describe('PledgeEditor', () => {
  let component: PledgeEditor;
  let fixture: ComponentFixture<PledgeEditor>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PledgeEditor]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PledgeEditor);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
