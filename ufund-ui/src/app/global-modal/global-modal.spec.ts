import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GlobalModal } from './global-modal';

describe('GlobalModal', () => {
  let component: GlobalModal;
  let fixture: ComponentFixture<GlobalModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GlobalModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GlobalModal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
