import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CupboardSearch } from './cupboard-search';

describe('CupboardSearch', () => {
  let component: CupboardSearch;
  let fixture: ComponentFixture<CupboardSearch>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CupboardSearch]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CupboardSearch);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
