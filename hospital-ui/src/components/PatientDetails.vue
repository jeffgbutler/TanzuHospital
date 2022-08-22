<template>
  <div v-if="patientId !== -1">
    <h2>Patient Details</h2>
    <p>Name: {{details.basicPatientInformation.firstName}} {{details.basicPatientInformation.lastName}},
      Gender: {{details.basicPatientInformation.gender}}
      Birth Date: {{details.basicPatientInformation.birthDate}}
    </p>
    <div v-if="details.allergies.length > 0">
      <h3>Allergies</h3>
      <b-table striped hover :items="details.allergies" />
    </div>
    <div v-if="details.encounters.length > 0">
      <h3>Encounters</h3>
      <b-table striped hover :items="details.encounters" />
    </div>

  </div>
</template>

<script>
export default {
  props: {
    patientId: -1
  },
  data() {
    return {
      details: Object
    }
  },
  mounted: function () {
    if (this.patientId !== -1) {
      fetch(`http://hospital-api.frontdoor.tanzuhospital.com/api/patients/${this.patientId}`, {method: 'GET'})
          .then(response => {
            return response.json();
          })
          .then(data => {
            this.details = data;
          });
    }
  }
}
</script>
