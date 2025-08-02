import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VerifyDoctorComponent } from './verify-doctor.component';

describe('VerifyDoctorComponent', () => {
  let component: VerifyDoctorComponent;
  let fixture: ComponentFixture<VerifyDoctorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VerifyDoctorComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(VerifyDoctorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
