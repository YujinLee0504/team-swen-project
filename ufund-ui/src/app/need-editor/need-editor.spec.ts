import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeedEditor } from './need-editor';

describe('NeedEditor', () => {
  let component: NeedEditor;
  let fixture: ComponentFixture<NeedEditor>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NeedEditor]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NeedEditor);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
