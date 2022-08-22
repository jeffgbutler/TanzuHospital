<template>
  <div>
    <h2>Search for Patients</h2>
    <b-form>
      <b-form-group label="First Name" label-for="firstName">
        <b-form-input id="firstName" v-model="firstName" />
      </b-form-group>
      <b-form-group label="Last Name" label-for="lastName">
        <b-form-input id="lastName" v-model="lastName" />
      </b-form-group>
    </b-form>
    <b-button-toolbar>
      <b-button variant="primary" @click="getPatients">Find Patients</b-button>
      <b-button variant="primary" @click="reset">Reset</b-button>
    </b-button-toolbar>
    <div class="d-flex justify-content-center mb-3">
      <b-spinner v-show="loading"/>
    </div>
    <div v-show="noMatches">
      <p> No Patients Matched</p>
    </div>
    <div v-show="errorString.length > 0">
      <p>Server error: {{ errorString }}</p>
    </div>
    <div v-if="patientData.length > 0">
      <b-table striped hover :items="patientData" />
    </div>
  </div>
</template>

<script>
  export default {
    data() {
      return {
        loading: false,
        noMatches: false,
        firstName: '',
        lastName: '',
        patientData: [],
        errorString: ''
      }
    },
    methods: {
      getPatients() {
        this.loading = true;
        this.noMatches = false;
        this.patientData = [];
        this.errorString = '';
        fetch(`http://hospital-api.frontdoor.tanzuhospital.com/api/patients?firstName=${this.firstName}&lastName=${this.lastName}`, {method: 'GET'})
            .then(this.handleResponse)
            .then((data) => {
              this.patientData = data;
              this.noMatches = data.length === 0;
              this.loading = false;
            })
            .catch((err) => {
              this.patientData = [];
              this.noMatches = false;
              this.loading = false;
              this.errorString = JSON.stringify(err);
            });
      },
      handleResponse(response) {
        return response.json()
            .then(json => {
              if (response.ok) {
                return json;
              } else {
                return Promise.reject({
                  status: response.status,
                  error: json.error,
                  error_description: json.error_description
                });
              }
            });
      },
      reset() {
        this.loading = false;
        this.noMatches = false;
        this.firstName = '';
        this.lastName = '';
        this.patientData = [];
        this.errorString = '';
      }
    }
  }
</script>
